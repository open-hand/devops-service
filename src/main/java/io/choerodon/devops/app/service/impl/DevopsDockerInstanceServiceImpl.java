package io.choerodon.devops.app.service.impl;

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
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.DockerDeployDTO;
import io.choerodon.devops.infra.dto.repo.DockerPullAccountDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.deploy.DockerInstanceStatusEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsDockerInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsHostAppInstanceRelMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.TypeUtil;

import org.hzero.core.base.BaseConstants;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

import static io.choerodon.devops.infra.constant.DevopsHostConstants.ERROR_SAVE_APP_HOST_REL_FAILED;

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


    @Override
    @Transactional
    public void deployDockerInstance(Long projectId, DockerDeployVO dockerDeployVO) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        // 1. 查询部署信息
        Long hostId = dockerDeployVO.getHostId();
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);

        DockerDeployDTO dockerDeployDTO = ConvertUtils.convertObject(dockerDeployVO, DockerDeployDTO.class);

        String deployObjectName = null;
        String deployVersion = null;
        Long appServiceId = null;

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(dockerDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());
        if (AppSourceType.MARKET.getValue().equals(dockerDeployVO.getSourceType())) {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(dockerDeployVO.getDeployObjectId()));
            if (Objects.isNull(marketServiceDeployObjectVO.getMarketHarborConfigVO())) {
                throw new CommonException("error.harbor.deploy.object.not.exist");
            }
            appServiceId = marketServiceDeployObjectVO.getMarketServiceId();

            MarketHarborConfigVO marketHarborConfigVO = marketServiceDeployObjectVO.getMarketHarborConfigVO();

            DockerPullAccountDTO dockerPullAccountDTO = new DockerPullAccountDTO();
            dockerPullAccountDTO.setPullAccount(marketHarborConfigVO.getRobotName());
            dockerPullAccountDTO.setPullPassword(marketHarborConfigVO.getToken());
            dockerPullAccountDTO.setHarborUrl(marketHarborConfigVO.getRepoUrl());

            //部署对象的名称
            deployObjectName = marketServiceDeployObjectVO.getDevopsAppServiceName();
            deployVersion = marketServiceDeployObjectVO.getDevopsAppServiceVersion();

            deploySourceVO.setMarketAppName(marketServiceDeployObjectVO.getMarketAppName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketAppVersion());
            deploySourceVO.setMarketServiceName(marketServiceDeployObjectVO.getMarketServiceName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketServiceVersion());
            //如果是市场部署将部署人员添加为应用的订阅人员
            marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());


        } else if (AppSourceType.CURRENT_PROJECT.getValue().equals(dockerDeployVO.getSourceType())) {
            deployVersion = dockerDeployVO.getImageInfo().getTag();
            deployObjectName = dockerDeployVO.getImageInfo().getImageName();

            HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(dockerDeployVO.getImageInfo().getRepoType(), TypeUtil.objToLong(dockerDeployVO.getImageInfo().getRepoId()), dockerDeployVO.getImageInfo().getImageName(), dockerDeployVO.getImageInfo().getTag());
            if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
                throw new CommonException(ERROR_IMAGE_TAG_NOT_FOUND);
            }
            dockerDeployDTO.setDockerPullAccountDTO(ConvertUtils.convertObject(imageTagVo, DockerPullAccountDTO.class));
            dockerDeployDTO.setImage(imageTagVo.getImageTagList().get(0).getPullCmd().replace("docker pull", ""));

            AppServiceDTO appServiceDTO = appServiceService.baseQueryByCode(deployObjectName, projectId);
            appServiceId = appServiceDTO == null ? null : appServiceDTO.getId();
        }

        // 2.保存记录
        DevopsDockerInstanceDTO devopsDockerInstanceDTO = ConvertUtils.convertObject(dockerDeployVO, DevopsDockerInstanceDTO.class);
        devopsDockerInstanceDTO.setStatus(DockerInstanceStatusEnum.OPERATING.value());
        devopsDockerInstanceDTO.setImage(dockerDeployDTO.getImage());
        MapperUtil.resultJudgedInsertSelective(devopsDockerInstanceMapper, devopsDockerInstanceDTO, ERROR_SAVE_DOCKER_INSTANCE_FAILED);

        // 保存应用实例关系
        if (appServiceId != null) {
            DevopsHostAppInstanceRelDTO devopsHostAppInstanceRelDTO = new DevopsHostAppInstanceRelDTO();
            devopsHostAppInstanceRelDTO.setInstanceId(devopsDockerInstanceDTO.getId());
            devopsHostAppInstanceRelDTO.setHostId(hostId);
            devopsHostAppInstanceRelDTO.setAppId(appServiceId);
            devopsHostAppInstanceRelDTO.setAppSource(dockerDeployVO.getSourceType());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppInstanceRelMapper, devopsHostAppInstanceRelDTO, ERROR_SAVE_APP_HOST_REL_FAILED);
        }


        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_DOCKER.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsDockerInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);


        dockerDeployDTO.setCmd(genCmd(dockerDeployDTO, dockerDeployVO.getValue()));
        dockerDeployDTO.setInstanceId(String.valueOf(devopsDockerInstanceDTO.getId()));

        // 3. 保存部署记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.MANUAL,
                null,
                DeployModeEnum.HOST,
                devopsHostDTO.getId(),
                devopsHostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.IMAGE,
                deployObjectName,
                deployVersion,
                null,
                deploySourceVO, DetailsHelper.getUserDetails().getUserId());

        // 4. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_DOCKER.value());
        hostAgentMsgVO.setKey(DevopsHostConstants.GROUP + hostId);
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerDeployDTO));

        LOGGER.info(">>>>>>>>>>>>>>>>>>>> deploy docker instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.DOCKER_INSTANCE, hostId, devopsDockerInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    private String genCmd(DockerDeployDTO dockerDeployDTO, String value) {
        String[] strings = value.split("\n");
        String values = "";
        for (String s : strings) {
            if (s.length() > 0 && !s.contains("#") && s.contains("docker")) {
                values = s;
            }
        }
        if (StringUtils.isEmpty(values) || !checkInstruction("image", values)) {
            throw new CommonException("error.instruction");
        }

        // 判断镜像是否存在 存在删除 部署
        return value;
    }

    private Boolean checkInstruction(String type, String instruction) {
        if (type.equals("jar")) {
            return instruction.contains("${jar}");
        } else {
            return instruction.contains("${containerName}") && instruction.contains("${imageName}") && instruction.contains(" -d ");
        }
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
}
