package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.DevopsClusterCommandConstants.PRIVATE_KEY_SAVE_PATH_TEMPLATE;
import static io.choerodon.devops.infra.constant.DevopsClusterCommandConstants.SAVE_PRIVATE_KEY_TEMPLATE;
import static io.choerodon.devops.infra.enums.DevopsMiddlewareTypeEnum.REDIS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.schmizz.sshj.SSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.AppServiceInstanceValidator;
import io.choerodon.devops.api.validator.MiddlewareConfigurationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsMiddlewareService;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.DevopsMiddlewareDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.HostAuthType;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
import io.choerodon.devops.infra.mapper.DevopsMiddlewareMapper;
import io.choerodon.devops.infra.util.*;

@Service
public class DevopsMiddlewareServiceImpl implements DevopsMiddlewareService {

    private static final String STAND_ALONE_CONFIG = "cluster:\n" +
            "  enabled: false\n";
    private static final String STANDALONE_MODE = "standalone";

    private static final String SENTINEL_MODE = "sentinel";

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsMiddlewareServiceImpl.class);

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private SshUtil sshUtil;
    @Autowired
    private DevopsHostMapper devopsHostMapper;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsMiddlewareMapper devopsMiddlewareMapper;

    /**
     * 中间件的环境部署逻辑和市场应用的部署逻辑完全一样，只是需要提前构造values
     *
     * @param projectId
     * @param middlewareRedisEnvDeployVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppServiceInstanceVO envDeployForRedis(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {

        // 根据部署模式以及版本查询部署部署对象id和市场服务id
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(REDIS.getType(), middlewareRedisEnvDeployVO.getMode(), middlewareRedisEnvDeployVO.getVersion());

        middlewareRedisEnvDeployVO.setMarketDeployObjectId(middlewareServiceReleaseInfo.getId());
        middlewareRedisEnvDeployVO.setMarketAppServiceId(middlewareServiceReleaseInfo.getMarketServiceId());


        // 如果是单机模式，需要添加 禁用集群模式配置
        if (STANDALONE_MODE.equals(middlewareRedisEnvDeployVO.getMode())) {
            middlewareRedisEnvDeployVO.setValues(STAND_ALONE_CONFIG + middlewareRedisEnvDeployVO.getValues());
        }

        MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = ConvertUtils.convertObject(middlewareRedisEnvDeployVO, MarketInstanceCreationRequestVO.class);

        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO);
    }

    @Override
    public void hostDeployForRedis(Long projectId, MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO) {
        checkMiddlewareName(projectId, middlewareRedisHostDeployVO.getName(), REDIS.getType());
        MiddlewareConfigurationValidator.validateRedisConfiguration(middlewareRedisHostDeployVO.getConfiguration());
        if (SENTINEL_MODE.equals(middlewareRedisHostDeployVO.getMode())) {
            CommonExAssertUtil.assertTrue(middlewareRedisHostDeployVO.getHostIds().size() >= 3, "error.host.size.less.than.3");
        }

        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(projectId, middlewareRedisHostDeployVO.getHostIds());

        if (SENTINEL_MODE.equals(middlewareRedisHostDeployVO.getMode())) {
            CommonExAssertUtil.assertTrue(devopsHostDTOList.size() >= 3, "error.host.size.less.than.3");
        }

        // 根据部署模式以及版本查询部署部署对象id和市场服务id
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(REDIS.getType(), middlewareRedisHostDeployVO.getMode(), middlewareRedisHostDeployVO.getVersion());


        LOGGER.info("========================================");
        LOGGER.info("start to deploy Middleware Redis,mode:{} version:{} projectId:{}", middlewareRedisHostDeployVO.getMode(), middlewareRedisHostDeployVO.getVersion(), projectId);
        SSHClient ssh = new SSHClient();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setDeployObjectId(middlewareServiceReleaseInfo.getId());
        deploySourceVO.setType(AppSourceType.MARKET.getValue());
        deploySourceVO.setProjectName(projectDTO.getName());
        DevopsHostDTO devopsHostDTOForConnection = devopsHostDTOList.get(0);

        HostConnectionVO hostConnectionVO = ConvertUtils.convertObject(devopsHostDTOForConnection, HostConnectionVO.class);
        SSHClient sshClient = new SSHClient();

        try {
            sshUtil.sshConnect(hostConnectionVO, sshClient);

            // 安装ansible、git初始化文件
            sshUtil.uploadPreProcessShell(sshClient, DeployObjectTypeEnum.MIDDLEWARE.value(), DeployObjectTypeEnum.MIDDLEWARE.value());

            // 创建节点密钥文件
            generateAndUploadPrivateKey(sshClient, devopsHostDTOList);

            // 生成并上传redis配置文件
            generateAndUploadRedisConfiguration(ssh, middlewareRedisHostDeployVO.getConfiguration());

            // 安装redis
            executeInstallRedis(sshClient, middlewareRedisHostDeployVO.getMode());

            devopsDeployRecordService.saveRecord(
                    projectId,
                    DeployType.MANUAL,
                    null,
                    DeployModeEnum.HOST,
                    devopsHostDTOForConnection.getId(),
                    devopsHostDTOForConnection.getName(),
                    PipelineStatus.SUCCESS.toValue(),
                    DeployObjectTypeEnum.MIDDLEWARE,
                    middlewareRedisHostDeployVO.getName(),
                    middlewareRedisHostDeployVO.getVersion(),
                    null,
                    deploySourceVO);

            // 保存中间件信息
            saveMiddlewareInfo(projectId,
                    middlewareRedisHostDeployVO.getName(),
                    REDIS.getType(),
                    middlewareRedisHostDeployVO.getMode(),
                    middlewareRedisHostDeployVO.getVersion(),
                    devopsHostDTOList.stream().map(h -> String.valueOf(h.getId())).collect(Collectors.joining(",")),
                    JsonHelper.marshalByJackson(middlewareRedisHostDeployVO.getConfiguration()));
            LOGGER.info("========================================");
            LOGGER.info("deploy Middleware Redis,mode:{} version:{} projectId:{}", middlewareRedisHostDeployVO.getMode(), middlewareRedisHostDeployVO.getVersion(), projectId);
        } catch (Exception e) {
            devopsDeployRecordService.saveRecord(
                    projectId,
                    DeployType.MANUAL,
                    null,
                    DeployModeEnum.HOST,
                    devopsHostDTOForConnection.getId(),
                    devopsHostDTOForConnection.getName(),
                    PipelineStatus.FAILED.toValue(),
                    DeployObjectTypeEnum.MIDDLEWARE,
                    middlewareRedisHostDeployVO.getName(),
                    middlewareRedisHostDeployVO.getVersion(),
                    null,
                    deploySourceVO);
            throw new CommonException("error.deploy.hostImage.failed.", e);
        } finally {
            sshUtil.closeSsh(ssh, null);
        }
    }

    @Override
    public void saveMiddlewareInfo(Long projectId, String name, String type, String mode, String version, String hostIds, String configuration) {
        DevopsMiddlewareDTO devopsMiddlewareDTO = new DevopsMiddlewareDTO(
                projectId,
                name,
                type,
                mode,
                version,
                hostIds,
                configuration
        );
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsMiddlewareMapper, devopsMiddlewareDTO, "error.middleware.insert");
    }

    private void generateAndUploadRedisConfiguration(SSHClient ssh, Map<String, String> Configuration) {

    }

    private void generateAndUploadPrivateKey(SSHClient ssh, List<DevopsHostDTO> devopsHostDTOList) throws IOException {
        // 创建目录
        sshUtil.execCommand(ssh, "mkdir -p /tmp/ansible/ssh-key");

        List<String> commands = new ArrayList<>();
        for (DevopsHostDTO devopsHostDTO : devopsHostDTOList) {
            if (HostAuthType.PUBLICKEY.value().equalsIgnoreCase(devopsHostDTO.getAuthType())) {
                commands.add(String.format(SAVE_PRIVATE_KEY_TEMPLATE, Base64Util.getBase64DecodedString(devopsHostDTO.getPassword()), String.format(PRIVATE_KEY_SAVE_PATH_TEMPLATE, devopsHostDTO.getName())));
            }
        }
        sshUtil.execCommands(ssh, commands);
    }

    private void executeInstallRedis(SSHClient sshClient, String mode) {

    }

    private void checkMiddlewareName(Long projectId, String name, String type) {
        AppServiceInstanceValidator.checkName(name);
        devopsMiddlewareMapper.checkNameUnique(projectId, name, type);
    }
}
