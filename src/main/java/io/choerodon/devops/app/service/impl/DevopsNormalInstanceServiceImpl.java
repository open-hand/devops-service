package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.choerodon.devops.api.vo.deploy.ConfigSettingVO;
import org.hzero.core.base.BaseConstants;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.market.JarReleaseConfigVO;
import io.choerodon.devops.api.vo.market.MarketMavenConfigVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.DevopsNormalInstanceDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.JarPullInfoDTO;
import io.choerodon.devops.infra.dto.repo.JavaDeployDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostInstanceType;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsNormalInstanceMapper;
import io.choerodon.devops.infra.util.HostDeployUtil;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
@Service
public class DevopsNormalInstanceServiceImpl implements DevopsNormalInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsNormalInstanceServiceImpl.class);

    private static final String ERROR_UPDATE_JAVA_INSTANCE_FAILED = "error.update.java.instance.failed";
    private static final String ERROR_JAR_VERSION_NOT_FOUND = "error.jar.version.not.found";

    @Autowired
    private DevopsNormalInstanceMapper devopsNormalInstanceMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    @Lazy
    private DevopsHostService devopsHostService;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    @Lazy
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsHostAppInstanceRelService devopsHostAppInstanceRelService;
    @Autowired
    private DeployConfigService deployConfigService;

    private static final BASE64Decoder decoder = new BASE64Decoder();

    @Override
    @Transactional
    public void deployJavaInstance(Long projectId, JarDeployVO jarDeployVO) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Long hostId = jarDeployVO.getHostId();
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(jarDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());

        deploySourceVO.setDeployObjectId(jarDeployVO.getDeployObjectId());

        try {
            jarDeployVO.setValue(new String(decoder.decodeBuffer(jarDeployVO.getValue()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.info("decode values failed!!!!. {}", jarDeployVO.getValue());
        }
        List<AppServiceDTO> appServiceDTOList;
        String deployObjectName = null;
        String deployVersion = null;
        // 0.1 查询部署信息

        // 0.3 获取并记录信息
        List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
        List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();

        // 标识部署对象
        String deployObjectKey;
        if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), jarDeployVO.getSourceType())
                || StringUtils.endsWithIgnoreCase(AppSourceType.HZERO.getValue(), jarDeployVO.getSourceType())) {
            deployObjectKey = String.valueOf(jarDeployVO.getDeployObjectId());
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(jarDeployVO.getDeployObjectId()));
            JarReleaseConfigVO jarReleaseConfigVO = JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getMarketJarLocation(), JarReleaseConfigVO.class);
            if (Objects.isNull(marketServiceDeployObjectVO.getMarketMavenConfigVO())) {
                throw new CommonException("error.maven.deploy.object.not.exist");
            }

            deployObjectName = marketServiceDeployObjectVO.getMarketServiceName();

            MarketMavenConfigVO marketMavenConfigVO = marketServiceDeployObjectVO.getMarketMavenConfigVO();
            C7nNexusComponentDTO nNexusComponentDTO = new C7nNexusComponentDTO();

            deployVersion = jarReleaseConfigVO.getVersion();
            nNexusComponentDTO.setName(jarReleaseConfigVO.getArtifactId());
            nNexusComponentDTO.setVersion(jarReleaseConfigVO.getVersion());
            nNexusComponentDTO.setGroup(jarReleaseConfigVO.getGroupId());
            nNexusComponentDTO.setDownloadUrl(getDownloadUrl(jarReleaseConfigVO));
            nexusComponentDTOList.add(nNexusComponentDTO);

            jarDeployVO.setProdJarInfoVO(new ProdJarInfoVO(jarReleaseConfigVO.getGroupId(),
                    jarReleaseConfigVO.getArtifactId(),
                    jarReleaseConfigVO.getVersion()));

            NexusMavenRepoDTO nexusMavenRepoDTO = new NexusMavenRepoDTO();
            nexusMavenRepoDTO.setNePullUserId(marketMavenConfigVO.getPullUserName());
            nexusMavenRepoDTO.setNePullUserPassword(marketMavenConfigVO.getPullPassword());
            mavenRepoDTOList.add(nexusMavenRepoDTO);
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(AppSourceType.HZERO.getValue(), jarDeployVO.getSourceType())) {
                AppServiceDTO appServiceDTO = new AppServiceDTO();
                appServiceDTO.setId(marketServiceDeployObjectVO.getMarketServiceId());
                appServiceDTO.setName(marketServiceDeployObjectVO.getMarketServiceName());
                appServiceDTOList = Arrays.asList(appServiceDTO);
            } else {
                appServiceDTOList = appServiceService.listByProjectIdAndGAV(projectId, jarReleaseConfigVO.getGroupId(), jarReleaseConfigVO.getArtifactId());

            }

            deploySourceVO.setMarketAppName(marketServiceDeployObjectVO.getMarketAppName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketAppVersion());
            deploySourceVO.setMarketServiceName(marketServiceDeployObjectVO.getMarketServiceName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketServiceVersion());

            //如果是市场部署将部署人员添加为应用的订阅人员
            marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());
        } else {
            // 0.2 从制品库获取仓库信息
            Long nexusRepoId = jarDeployVO.getProdJarInfoVO().getRepositoryId();
            String groupId = jarDeployVO.getProdJarInfoVO().getGroupId();
            String artifactId = jarDeployVO.getProdJarInfoVO().getArtifactId();
            String version = jarDeployVO.getProdJarInfoVO().getVersion();
            nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectId, nexusRepoId, groupId, artifactId, version);
            mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectId, Collections.singleton(nexusRepoId));
            deployObjectName = nexusComponentDTOList.get(0).getName();
            deployVersion = nexusComponentDTOList.get(0).getVersion();

            appServiceDTOList = appServiceService.listByProjectIdAndGAV(projectId, groupId, artifactId);

            deployObjectKey = new StringBuilder()
                    .append(nexusRepoId)
                    .append(BaseConstants.Symbol.COLON)
                    .append(groupId)
                    .append(BaseConstants.Symbol.COLON)
                    .append(artifactId)
                    .toString();
        }
        if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
            throw new CommonException(ERROR_JAR_VERSION_NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
            throw new CommonException("error.get.maven.config");
        }

        JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO();
        jarPullInfoDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
        jarPullInfoDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
        jarPullInfoDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());

        // 2.保存记录
        DevopsNormalInstanceDTO devopsNormalInstanceDTO = queryByHostIdAndName(hostId, jarDeployVO.getName());
        if (devopsNormalInstanceDTO == null) {
            devopsNormalInstanceDTO = new DevopsNormalInstanceDTO(hostId,
                    jarDeployVO.getName(),
                    jarDeployVO.getSourceType(),
                    HostResourceType.JAVA_PROCESS.value());

            MapperUtil.resultJudgedInsertSelective(devopsNormalInstanceMapper, devopsNormalInstanceDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);

        } else {
            // 删除原有应用关联关系
            devopsHostAppInstanceRelService.deleteByHostIdAndInstanceInfo(hostId, devopsNormalInstanceDTO.getId(), HostInstanceType.NORMAL_PROCESS.value());
        }
        // 有关联的应用，则保存关联关系
        if (!CollectionUtils.isEmpty(appServiceDTOList)) {
            Set<Long> appIds = appServiceDTOList.stream().map(AppServiceDTO::getId).collect(Collectors.toSet());
            Map<Long, AppServiceDTO> appServiceDTOMap = appServiceDTOList.stream().collect(Collectors.toMap(AppServiceDTO::getId, Function.identity()));
            Long instanceId = devopsNormalInstanceDTO.getId();
            appIds.forEach(appId -> devopsHostAppInstanceRelService.saveHostAppInstanceRel(projectId,
                    hostId,
                    appId,
                    jarDeployVO.getSourceType(),
                    instanceId,
                    HostInstanceType.NORMAL_PROCESS.value(), appServiceDTOMap.get(appId) == null ? null : appServiceDTOMap.get(appId).getName()));
        }


        JavaDeployDTO javaDeployDTO = new JavaDeployDTO(jarPullInfoDTO,
                jarDeployVO.getName(),
                deployObjectName,
                String.valueOf(devopsNormalInstanceDTO.getId()),
                HostDeployUtil.genJavaRunCmd(jarPullInfoDTO, jarDeployVO, devopsNormalInstanceDTO.getId()),
                devopsNormalInstanceDTO.getPid());

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_JAR.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.JAVA_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsNormalInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        // 保存执行记录
        Long devopsDeployRecordId = devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.MANUAL,
                null,
                DeployModeEnum.HOST,
                devopsHostDTO.getId(),
                devopsHostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.JAR,
                deployObjectName,
                deployVersion,
                null,
                deploySourceVO);

        // 保存用户设置部署配置文件
        deployConfigService.saveConfigSetting(projectId, devopsDeployRecordId, deployObjectKey, jarDeployVO);

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_JAR.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaDeployDTO));
        hostAgentMsgVO.setConfigSetting(deployConfigService.doCreateConfigSetting(projectId, jarDeployVO));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.JAVA_INSTANCE, hostId, devopsNormalInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    @Override
    public List<DevopsNormalInstanceDTO> listByHostId(Long hostId) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        return devopsNormalInstanceMapper.listByHostId(hostId);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsNormalInstanceDTO devopsNormalInstanceDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsNormalInstanceMapper, devopsNormalInstanceDTO, ERROR_UPDATE_JAVA_INSTANCE_FAILED);
    }

    @Override
    @Transactional
    public void baseDelete(Long instanceId) {
        devopsNormalInstanceMapper.deleteByPrimaryKey(instanceId);
    }

    @Override
    public DevopsNormalInstanceDTO baseQuery(Long instanceId) {
        return devopsNormalInstanceMapper.selectByPrimaryKey(instanceId);
    }

    @Override
    public DevopsNormalInstanceDTO queryByHostIdAndName(Long hostId, String name) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.ERROR_JAR_NAME_IS_NULL);
        DevopsNormalInstanceDTO devopsNormalInstanceDTO = new DevopsNormalInstanceDTO(hostId, name);
        return devopsNormalInstanceMapper.selectOne(devopsNormalInstanceDTO);
    }

    private String getDownloadUrl(JarReleaseConfigVO jarReleaseConfigVO) {
        //拼接download URL http://xxxx:17145/repository/lilly-snapshot/io/choerodon/springboot/0.0.1-SNAPSHOT/springboot-0.0.1-20210106.020444-2.jar
        return jarReleaseConfigVO.getNexusRepoUrl() + BaseConstants.Symbol.SLASH +
                jarReleaseConfigVO.getGroupId().replace(".", "/") +
                BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getArtifactId() + BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getVersion() +
                BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getArtifactId() + BaseConstants.Symbol.MIDDLE_LINE + jarReleaseConfigVO.getSnapshotTimestamp() + ".jar";
    }
}
