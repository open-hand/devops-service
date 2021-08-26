package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.AssertUtils;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import sun.misc.BASE64Decoder;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoImageTagVo;
import io.choerodon.devops.api.vo.market.MarketHarborConfigVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.DockerDeployDTO;
import io.choerodon.devops.infra.dto.repo.DockerPullAccountDTO;
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
import io.choerodon.devops.infra.mapper.DevopsDockerInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsHostAppInstanceRelMapper;
import io.choerodon.devops.infra.util.HostDeployUtil;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 14:13
 */
@Service
public class DevopsDockerInstanceServiceImpl implements DevopsDockerInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsDockerInstanceServiceImpl.class);

    private static final String ERROR_SAVE_DOCKER_INSTANCE_FAILED = "error.save.docker.instance.failed";
    private static final String ERROR_UPDATE_DOCKER_INSTANCE_FAILED = "error.update.docker.instance.failed";
    private static final String ERROR_IMAGE_TAG_NOT_FOUND = "error.image.tag.not.found";

    @Autowired
    private DevopsDockerInstanceMapper devopsDockerInstanceMapper;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private DevopsHostService devopsHostService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private DevopsHostAppInstanceRelMapper devopsHostAppInstanceRelMapper;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    private DevopsHostAppInstanceRelService devopsHostAppInstanceRelService;


    private static final BASE64Decoder decoder = new BASE64Decoder();


    @Override
    @Transactional
    public void deployDockerInstance(Long projectId, DockerDeployVO dockerDeployVO) {
        //1.获取项目信息
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        //2.获取主机信息
        DevopsHostDTO hostDTO = getHost(dockerDeployVO.getHostId());
        checkHostExist(hostDTO);


        String deployObjectName = null;
        String deployVersion = null;
        Long appServiceId = null;
        String serviceName = null;
        DockerDeployDTO dockerDeployDTO = ConvertUtils.convertObject(dockerDeployVO, DockerDeployDTO.class);

        DeploySourceVO deploySourceVO = initDeploySourceVO(dockerDeployVO, projectDTO);

        if (isMarketOrHzero(dockerDeployVO)) {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = getMarketServiceDeployObjectVO(projectId, dockerDeployVO);
            MarketHarborConfigVO marketHarborConfigVO = marketServiceDeployObjectVO.getMarketHarborConfigVO();
            DockerPullAccountDTO dockerPullAccountDTO = initDockerPullAccountDTO(marketHarborConfigVO);
            dockerDeployDTO = initMarketDockerDeployDTO(dockerDeployDTO, dockerPullAccountDTO, marketServiceDeployObjectVO);
            appServiceId = marketServiceDeployObjectVO.getMarketServiceId();
            if (AppSourceType.HZERO.getValue().equals(dockerDeployVO.getSourceType())) {
                deployObjectName = marketServiceDeployObjectVO.getMarketServiceName();
                deployVersion = marketServiceDeployObjectVO.getMarketServiceVersion();
            } else {
                //部署对象的名称
                deployObjectName = marketServiceDeployObjectVO.getDevopsAppServiceName();
                deployVersion = marketServiceDeployObjectVO.getDevopsAppServiceVersion();
            }
            serviceName = marketServiceDeployObjectVO.getMarketServiceName();
            fillDeploySource(deploySourceVO, marketServiceDeployObjectVO);
            //如果是市场部署将部署人员添加为应用的订阅人员
            marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());
        } else if (AppSourceType.CURRENT_PROJECT.getValue().equals(dockerDeployVO.getSourceType())) {
            HarborC7nRepoImageTagVo imageTagVo = getHarborC7nRepoImageTagVo(dockerDeployVO);
            dockerDeployDTO = initProjectDockerDeployDTO(dockerDeployDTO, imageTagVo);

            deployVersion = dockerDeployVO.getImageInfo().getTag();
            deployObjectName = dockerDeployVO.getImageInfo().getImageName();
            AppServiceDTO appServiceDTO = appServiceService.baseQueryByCode(deployObjectName, projectId);
            appServiceId = appServiceDTO == null ? null : appServiceDTO.getId();
            serviceName = appServiceDTO == null ? null : appServiceDTO.getName();
        }

        // 2.保存记录
        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.queryByHostIdAndName(hostDTO.getId(), dockerDeployDTO.getName());
        devopsDockerInstanceDTO = saveDevopsDockerInstanceDTO(projectId, dockerDeployVO, hostDTO, dockerDeployDTO, appServiceId, serviceName, devopsDockerInstanceDTO);
        DevopsHostCommandDTO devopsHostCommandDTO = saveDevopsHostCommandDTO(hostDTO, devopsDockerInstanceDTO);
        String values = getDeValues(dockerDeployVO);

        dockerDeployDTO.setCmd(HostDeployUtil.genDockerRunCmd(dockerDeployDTO, values));
        dockerDeployDTO.setInstanceId(String.valueOf(devopsDockerInstanceDTO.getId()));

        // 3. 保存部署记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.MANUAL,
                null,
                DeployModeEnum.HOST,
                hostDTO.getId(),
                hostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.IMAGE,
                deployObjectName,
                deployVersion,
                null,
                deploySourceVO);

        // 4. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = initHostAgentMsg(hostDTO, dockerDeployDTO, devopsHostCommandDTO);

        sendHostDeployMsg(hostDTO, devopsDockerInstanceDTO, hostAgentMsgVO);

    }

    private MarketServiceDeployObjectVO getMarketServiceDeployObjectVO(Long projectId, DockerDeployVO dockerDeployVO) {
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(dockerDeployVO.getDeployObjectId()));
        if (Objects.isNull(marketServiceDeployObjectVO.getMarketHarborConfigVO())) {
            throw new CommonException("error.harbor.deploy.object.not.exist");
        }
        return marketServiceDeployObjectVO;
    }

    private void fillDeploySource(DeploySourceVO deploySourceVO, MarketServiceDeployObjectVO marketServiceDeployObjectVO) {
        deploySourceVO.setMarketAppName(marketServiceDeployObjectVO.getMarketAppName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketAppVersion());
        deploySourceVO.setMarketServiceName(marketServiceDeployObjectVO.getMarketServiceName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketServiceVersion());
    }

    private HarborC7nRepoImageTagVo getHarborC7nRepoImageTagVo(DockerDeployVO dockerDeployVO) {
        HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(dockerDeployVO.getImageInfo().getRepoType(), TypeUtil.objToLong(dockerDeployVO.getImageInfo().getRepoId()), dockerDeployVO.getImageInfo().getImageName(), dockerDeployVO.getImageInfo().getTag());
        if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
            throw new CommonException(ERROR_IMAGE_TAG_NOT_FOUND);
        }
        return imageTagVo;
    }

    private DockerDeployDTO initProjectDockerDeployDTO(DockerDeployDTO dockerDeployDTO, HarborC7nRepoImageTagVo imageTagVo) {

        dockerDeployDTO.setDockerPullAccountDTO(ConvertUtils.convertObject(imageTagVo, DockerPullAccountDTO.class));
        dockerDeployDTO.setImage(imageTagVo.getImageTagList().get(0).getPullCmd().replace("docker pull", ""));
        return dockerDeployDTO;
    }

    private DockerDeployDTO initMarketDockerDeployDTO(DockerDeployDTO dockerDeployDTO, DockerPullAccountDTO dockerPullAccountDTO, MarketServiceDeployObjectVO marketServiceDeployObjectVO) {
        dockerDeployDTO.setDockerPullAccountDTO(dockerPullAccountDTO);
        dockerDeployDTO.setImage(marketServiceDeployObjectVO.getMarketDockerImageUrl());
        return dockerDeployDTO;
    }

    private DevopsDockerInstanceDTO saveDevopsDockerInstanceDTO(Long projectId, DockerDeployVO dockerDeployVO, DevopsHostDTO hostDTO, DockerDeployDTO dockerDeployDTO, Long appServiceId, String serviceName, DevopsDockerInstanceDTO devopsDockerInstanceDTO) {
        if (devopsDockerInstanceDTO == null) {
            devopsDockerInstanceDTO = ConvertUtils.convertObject(dockerDeployVO, DevopsDockerInstanceDTO.class);
            devopsDockerInstanceDTO.setImage(dockerDeployDTO.getImage());
            MapperUtil.resultJudgedInsertSelective(devopsDockerInstanceMapper, devopsDockerInstanceDTO, ERROR_SAVE_DOCKER_INSTANCE_FAILED);
            // 保存应用实例关系
            saveHostInstanceRel(projectId, dockerDeployVO, hostDTO, appServiceId, serviceName, devopsDockerInstanceDTO);
        } else {
            dockerDeployDTO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        }
        return devopsDockerInstanceDTO;
    }

    private void sendHostDeployMsg(DevopsHostDTO hostDTO, DevopsDockerInstanceDTO devopsDockerInstanceDTO, HostAgentMsgVO hostAgentMsgVO) {
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostDTO.getId(),
                String.format(DevopsHostConstants.DOCKER_INSTANCE, hostDTO.getId(), devopsDockerInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    private HostAgentMsgVO initHostAgentMsg(DevopsHostDTO hostDTO, DockerDeployDTO dockerDeployDTO, DevopsHostCommandDTO devopsHostCommandDTO) {
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostDTO.getId()));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_DOCKER.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerDeployDTO));
        LOGGER.info(">>>>>>>>>>>>>>>>>>>> deploy docker instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        return hostAgentMsgVO;
    }

    private String getDeValues(DockerDeployVO dockerDeployVO) {
        String values = null;
        try {
            values = new String(decoder.decodeBuffer(dockerDeployVO.getValue()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.info("decode values failed!!!!. {}", dockerDeployVO.getValue());
        }
        return values;
    }

    private DevopsHostCommandDTO saveDevopsHostCommandDTO(DevopsHostDTO hostDTO, DevopsDockerInstanceDTO devopsDockerInstanceDTO) {
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_DOCKER.value());
        devopsHostCommandDTO.setHostId(hostDTO.getId());
        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsDockerInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);
        return devopsHostCommandDTO;
    }

    private void saveHostInstanceRel(Long projectId, DockerDeployVO dockerDeployVO, DevopsHostDTO hostDTO, Long appServiceId, String serviceName, DevopsDockerInstanceDTO devopsDockerInstanceDTO) {
        if (appServiceId != null) {
            devopsHostAppInstanceRelService.saveHostAppInstanceRel(projectId,
                    hostDTO.getId(),
                    appServiceId,
                    dockerDeployVO.getSourceType(),
                    devopsDockerInstanceDTO.getId(),
                    HostInstanceType.DOCKER_PROCESS.value(), serviceName);
        }
    }

    private DockerPullAccountDTO initDockerPullAccountDTO(MarketHarborConfigVO marketHarborConfigVO) {
        DockerPullAccountDTO dockerPullAccountDTO = new DockerPullAccountDTO()
                .setHarborUrl(marketHarborConfigVO.getRepoUrl())
                .setPullAccount(marketHarborConfigVO.getRobotName())
                .setPullPassword(marketHarborConfigVO.getToken());
        return dockerPullAccountDTO;
    }

    private boolean isMarketOrHzero(DockerDeployVO dockerDeployVO) {
        return AppSourceType.MARKET.getValue().equals(dockerDeployVO.getSourceType())
                || AppSourceType.HZERO.getValue().equals(dockerDeployVO.getSourceType());
    }

    private DeploySourceVO initDeploySourceVO(DockerDeployVO dockerDeployVO, ProjectDTO projectDTO) {
        DeploySourceVO deploySourceVO = new DeploySourceVO()
                .setType(dockerDeployVO.getSourceType())
                .setProjectName(projectDTO.getName());
        return deploySourceVO;
    }

    private void checkHostExist(DevopsHostDTO hostDTO) {
        AssertUtils.notNull(hostDTO, "error.host.not.exist");
    }

    private DevopsHostDTO getHost(Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);
        return devopsHostDTO;
    }

    @Override
    public DevopsDockerInstanceDTO baseQuery(Long instanceId) {
        return devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsDockerInstanceDTO devopsDockerInstanceDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDockerInstanceMapper, devopsDockerInstanceDTO, ERROR_UPDATE_DOCKER_INSTANCE_FAILED);
    }

    @Override
    @Transactional
    public void baseDelete(Long instanceId) {
        devopsDockerInstanceMapper.deleteByPrimaryKey(instanceId);
    }

    @Override
    public List<DevopsDockerInstanceDTO> listByHostId(Long hostId) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);

        return devopsDockerInstanceMapper.listByHostId(hostId);
    }

    @Override
    public DevopsDockerInstanceDTO queryByHostIdAndName(Long hostId, String containerName) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        Assert.notNull(containerName, ResourceCheckConstant.ERROR_CONTAINER_NAME_IS_NULL);
        return devopsDockerInstanceMapper.selectOne(new DevopsDockerInstanceDTO(hostId, containerName));
    }
}
