package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoImageTagVo;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsDockerInstanceService;
import io.choerodon.devops.app.service.DevopsHostCommandService;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
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
import io.choerodon.devops.infra.enums.deploy.DockerInstanceStatusEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsDockerInstanceMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 14:13
 */
@Service
public class DevopsDockerInstanceServiceImpl implements DevopsDockerInstanceService {

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


    @Override
    @Transactional
    public void deployDockerInstance(Long projectId, DockerDeployVO dockerDeployVO) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        // 1. 查询部署信息
        Long hostId = dockerDeployVO.getHostId();
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);

        DockerDeployDTO dockerDeployDTO = ConvertUtils.convertObject(dockerDeployVO, DockerDeployDTO.class);

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(dockerDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());
        if (AppSourceType.MARKET.getValue().equals(dockerDeployVO.getSourceType())) {



        } else if (AppSourceType.CURRENT_PROJECT.getValue().equals(dockerDeployVO.getSourceType())) {
            HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(dockerDeployVO.getImageInfo().getRepoType(), TypeUtil.objToLong(dockerDeployVO.getImageInfo().getRepoId()), dockerDeployVO.getImageInfo().getImageName(), dockerDeployVO.getImageInfo().getTag());
            if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
                throw new CommonException(ERROR_IMAGE_TAG_NOT_FOUND);
            }
            dockerDeployDTO.setDockerPullAccountDTO(ConvertUtils.convertObject(imageTagVo, DockerPullAccountDTO.class));
            dockerDeployDTO.setImage(imageTagVo.getImageTagList().get(0).getPullCmd().replace("docker pull", ""));

        }

        // 2.保存记录
        DevopsDockerInstanceDTO devopsDockerInstanceDTO = ConvertUtils.convertObject(dockerDeployVO, DevopsDockerInstanceDTO.class);
        devopsDockerInstanceDTO.setStatus(DockerInstanceStatusEnum.OPERATING.value());
        devopsDockerInstanceDTO.setImage(dockerDeployDTO.getImage());
        MapperUtil.resultJudgedInsertSelective(devopsDockerInstanceMapper, devopsDockerInstanceDTO, ERROR_SAVE_DOCKER_INSTANCE_FAILED);


        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.START_DOCKER.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsDockerInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(hostId);
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_DOCKER.value());
        hostAgentMsgVO.setKey(DevopsHostConstants.GROUP + hostId);
        hostAgentMsgVO.setCommandId(devopsHostCommandDTO.getId());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerDeployDTO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.DOCKER_INSTANCE, hostId, devopsDockerInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));

        // 4. 保存部署记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.MANUAL,
                null,
                DeployModeEnum.HOST,
                devopsHostDTO.getId(),
                devopsHostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.IMAGE,
                dockerDeployVO.getImageInfo().getImageName(),
                dockerDeployVO.getImageInfo().getTag(),
                null,
                deploySourceVO, DetailsHelper.getUserDetails().getUserId());
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

        DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
        devopsDockerInstanceDTO.setHostId(hostId);
        return devopsDockerInstanceMapper.select(devopsDockerInstanceDTO);
    }
}
