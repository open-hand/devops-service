package io.choerodon.devops.app.service.impl;

import static org.springframework.util.Assert.notNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.DockerProcessUpdatePayload;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.hrds.HarborC7nRepoImageTagVo;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
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
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.enums.host.DevopsHostDeployType;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.handler.HostConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDockerInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsHostAppMapper;
import io.choerodon.devops.infra.util.*;

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

    private static final String ERROR_SAVE_DOCKER_INSTANCE_FAILED = "devops.save.docker.instance.failed";
    private static final String ERROR_UPDATE_DOCKER_INSTANCE_FAILED = "devops.update.docker.instance.failed";
    private static final String ERROR_IMAGE_TAG_NOT_FOUND = "devops.image.tag.not.found";

    private static final String CREATE = "create";

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
    private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    private DevopsHostAppMapper devopsHostAppMapper;
    @Autowired
    private DevopsHostUserPermissionService devopsHostUserPermissionService;
    @Autowired
    private DevopsCiHostDeployInfoService devopsCiHostDeployInfoService;
    @Autowired
    private HostConnectionHandler hostConnectionHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deployDockerInstance(Long projectId, DockerDeployVO dockerDeployVO) {
        //1.获取项目信息
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        //2.获取主机信息
        DevopsHostDTO hostDTO = getHost(dockerDeployVO.getHostId());
        notNull(hostDTO, "devops.host.not.exist");
        //校验主机权限
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, hostDTO, DetailsHelper.getUserDetails().getUserId());
        // 校验主机已连接
        hostConnectionHandler.checkHostConnection(dockerDeployVO.getHostId());
        //获取主机应用
        DevopsHostAppDTO devopsHostAppDTO = getDevopsHostAppDTO(projectId, dockerDeployVO, hostDTO.getId());
        if (devopsHostAppDTO == null) {
            return;
        }
        //初始化部署来源
        DeploySourceVO deploySourceVO = initDeploySourceVO(dockerDeployVO, projectDTO);
        //获取部署对象
        DockerDeployDTO dockerDeployDTO = getDockerDeployDTO(dockerDeployVO);
        dockerDeployDTO.setVersion(devopsHostAppDTO.getVersion());
        dockerDeployDTO.setAppCode(devopsHostAppDTO.getCode());

        // 保存实例的信息
        DevopsDockerInstanceDTO devopsDockerInstanceDTO = createDockerInstanceDTO(dockerDeployVO, devopsHostAppDTO, dockerDeployDTO);

        // 如果该应用关联了流水线，同步修改流水线里面的信息
        devopsCiHostDeployInfoService.updateDockerDeployInfoFromAppCenter(dockerDeployVO);

        //保存命令
        DevopsHostCommandDTO devopsHostCommandDTO = saveDevopsHostCommandDTO(hostDTO, devopsDockerInstanceDTO);

        //保存部署记录
        saveDeployRecord(projectId, dockerDeployVO, hostDTO, devopsHostAppDTO, deploySourceVO, dockerDeployDTO, devopsDockerInstanceDTO);

        // 4. 发送部署指令给agent
        dockerDeployDTO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        HostAgentMsgVO hostAgentMsgVO = initHostAgentMsg(hostDTO, dockerDeployDTO, devopsHostCommandDTO);

        sendHostDeployMsg(hostDTO, devopsDockerInstanceDTO, hostAgentMsgVO);

    }

    private void saveDeployRecord(Long projectId, DockerDeployVO dockerDeployVO, DevopsHostDTO hostDTO, DevopsHostAppDTO devopsHostAppDTO, DeploySourceVO deploySourceVO, DockerDeployDTO dockerDeployDTO, DevopsDockerInstanceDTO devopsDockerInstanceDTO) {
        String deployObjectName = null;
        String deployVersion = null;
        deployVersion = dockerDeployVO.getImageInfo().getTag();
        deployObjectName = dockerDeployVO.getImageInfo().getImageName();
        dockerDeployDTO.setCmd(HostDeployUtil.genDockerRunCmd(dockerDeployDTO, Base64Util.decodeBuffer(devopsDockerInstanceDTO.getDockerCommand())));
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
                DeployObjectTypeEnum.DOCKER,
                deployObjectName,
                deployVersion,
                devopsHostAppDTO.getName(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getId(),
                deploySourceVO);
    }

    @NotNull
    private DevopsDockerInstanceDTO createDockerInstanceDTO(DockerDeployVO dockerDeployVO, DevopsHostAppDTO devopsHostAppDTO, DockerDeployDTO dockerDeployDTO) {
        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.queryByHostIdAndName(devopsHostAppDTO.getHostId(), dockerDeployVO.getContainerName());
        if (devopsDockerInstanceDTO == null) {
            devopsDockerInstanceDTO = ConvertUtils.convertObject(dockerDeployVO, DevopsDockerInstanceDTO.class);
            devopsDockerInstanceDTO.setName(dockerDeployVO.getContainerName());
            devopsDockerInstanceDTO.setImage(dockerDeployDTO.getImage());
            devopsDockerInstanceDTO.setAppId(devopsHostAppDTO.getId());
            devopsDockerInstanceDTO.setRepoName(dockerDeployDTO.getRepoName());
            devopsDockerInstanceDTO.setRepoType(dockerDeployDTO.getRepoType());
            devopsDockerInstanceDTO.setRepoId(dockerDeployDTO.getRepoId());
            devopsDockerInstanceDTO.setImageName(dockerDeployDTO.getImageName());
            devopsDockerInstanceDTO.setTag(dockerDeployDTO.getTag());
            devopsDockerInstanceDTO.setUserName(dockerDeployDTO.getUserName());
            devopsDockerInstanceDTO.setPassWord(dockerDeployDTO.getPassWord());
            devopsDockerInstanceDTO.setPrivateRepository(dockerDeployDTO.getPrivateRepository());
            devopsDockerInstanceDTO.setDockerCommand(dockerDeployVO.getValue());
            MapperUtil.resultJudgedInsertSelective(devopsDockerInstanceMapper, devopsDockerInstanceDTO, ERROR_SAVE_DOCKER_INSTANCE_FAILED);
            return devopsDockerInstanceDTO;
        } else {
            devopsDockerInstanceDTO.setName(dockerDeployVO.getContainerName());
            devopsDockerInstanceDTO.setImage(dockerDeployDTO.getImage());
            devopsDockerInstanceDTO.setAppId(devopsHostAppDTO.getId());
            devopsDockerInstanceDTO.setRepoName(dockerDeployDTO.getRepoName());
            devopsDockerInstanceDTO.setRepoType(dockerDeployDTO.getRepoType());
            devopsDockerInstanceDTO.setRepoId(dockerDeployDTO.getRepoId());
            devopsDockerInstanceDTO.setImageName(dockerDeployDTO.getImageName());
            devopsDockerInstanceDTO.setTag(dockerDeployDTO.getTag());
            devopsDockerInstanceDTO.setUserName(dockerDeployDTO.getUserName());
            devopsDockerInstanceDTO.setPassWord(dockerDeployDTO.getPassWord());
            devopsDockerInstanceDTO.setPrivateRepository(dockerDeployDTO.getPrivateRepository());
            devopsDockerInstanceDTO.setDockerCommand(dockerDeployVO.getValue());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsDockerInstanceMapper, devopsDockerInstanceDTO, ERROR_UPDATE_DOCKER_INSTANCE_FAILED);
            return devopsDockerInstanceMapper.selectByPrimaryKey(devopsDockerInstanceDTO.getId());
        }
    }

    private DockerDeployDTO getDockerDeployDTO(DockerDeployVO dockerDeployVO) {
        DockerDeployDTO dockerDeployDTO = ConvertUtils.convertObject(dockerDeployVO, DockerDeployDTO.class);
        //目前只支持项目下的部署
        // 从制品库找到的镜像
        if (DevopsHostDeployType.DEFAULT.value().equals(dockerDeployVO.getRepoType())) {
            HarborC7nRepoImageTagVo imageTagVo = getHarborC7nRepoImageTagVo(dockerDeployVO);
            dockerDeployDTO = initProjectDockerDeployDTO(dockerDeployDTO, imageTagVo, dockerDeployVO);
        } else if (DevopsHostDeployType.CUSTOM.value().equals(dockerDeployVO.getRepoType())) {
            dockerDeployDTO = initCustomDockerDeployDTO(dockerDeployDTO, dockerDeployVO);
        } else {
            throw new CommonException("devops.unsupported.image.source");
        }
        return dockerDeployDTO;
    }

    private DevopsHostAppDTO getDevopsHostAppDTO(Long projectId, DockerDeployVO dockerDeployVO, Long hostId) {
        if (StringUtils.equals(CREATE, dockerDeployVO.getOperation())) {
            //插入主机应用实例
            DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO();
            devopsHostAppDTO.setRdupmType(RdupmTypeEnum.DOCKER.value());
            devopsHostAppDTO.setProjectId(projectId);
            devopsHostAppDTO.setHostId(hostId);
            devopsHostAppDTO.setName(dockerDeployVO.getAppName());
            devopsHostAppDTO.setCode(dockerDeployVO.getAppCode());
            devopsHostAppDTO.setOperationType(OperationTypeEnum.CREATE_APP.value());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, "devops.save.host.app");
            return devopsHostAppMapper.selectByPrimaryKey(devopsHostAppDTO.getId());
        } else {
            //查询主机应用实例
            DevopsHostAppDTO devopsHostAppDTO = devopsHostAppMapper.selectByPrimaryKey(dockerDeployVO.getHostAppId());
            devopsHostAppDTO.setName(dockerDeployVO.getName());
            devopsHostAppMapper.updateByPrimaryKey(devopsHostAppDTO);
            return devopsHostAppMapper.selectByPrimaryKey(dockerDeployVO.getHostAppId());
        }
    }

    private DockerDeployDTO initCustomDockerDeployDTO(DockerDeployDTO dockerDeployDTO, DockerDeployVO dockerDeployVO) {
        dockerDeployDTO.setImage(dockerDeployVO.getExternalImageInfo().getImageUrl());
        dockerDeployDTO.setDockerPullAccountDTO(new DockerPullAccountDTO(dockerDeployVO.getExternalImageInfo().getImageUrl(),
                dockerDeployVO.getExternalImageInfo().getUsername(),
                dockerDeployVO.getExternalImageInfo().getPassword()));
        dockerDeployDTO.setRepoType(DevopsHostDeployType.CUSTOM.value());
        dockerDeployDTO.setPrivateRepository(dockerDeployVO.getExternalImageInfo().getPrivateRepository());
        dockerDeployDTO.setUserName(dockerDeployVO.getExternalImageInfo().getUsername());
        dockerDeployDTO.setPassWord(dockerDeployVO.getExternalImageInfo().getPassword());
        return dockerDeployDTO;
    }

    private DevopsHostDTO getHost(Long hostId) {
        return devopsHostService.baseQuery(hostId);
    }

    private HarborC7nRepoImageTagVo getHarborC7nRepoImageTagVo(DockerDeployVO dockerDeployVO) {
        HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(dockerDeployVO.getRepoType(), TypeUtil.objToLong(dockerDeployVO.getImageInfo().getRepoId()), dockerDeployVO.getImageInfo().getImageName(), dockerDeployVO.getImageInfo().getTag());
        if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
            throw new CommonException(ERROR_IMAGE_TAG_NOT_FOUND);
        }
        return imageTagVo;
    }

    private DockerDeployDTO initProjectDockerDeployDTO(DockerDeployDTO dockerDeployDTO, HarborC7nRepoImageTagVo imageTagVo, DockerDeployVO dockerDeployVO) {

        dockerDeployDTO.setDockerPullAccountDTO(ConvertUtils.convertObject(imageTagVo, DockerPullAccountDTO.class));
        dockerDeployDTO.setImage(imageTagVo.getImageTagList().get(0).getPullCmd().replace("docker pull", ""));
        dockerDeployDTO.setRepoName(dockerDeployVO.getImageInfo().getRepoName());
        dockerDeployDTO.setRepoType(DevopsHostDeployType.DEFAULT.value());
        dockerDeployDTO.setRepoId(dockerDeployVO.getImageInfo().getRepoId());
        dockerDeployDTO.setImageName(dockerDeployVO.getImageInfo().getImageName());
        dockerDeployDTO.setTag(dockerDeployVO.getImageInfo().getTag());
        return dockerDeployDTO;
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
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>> deploy docker instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }
        return hostAgentMsgVO;
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

    private DeploySourceVO initDeploySourceVO(DockerDeployVO dockerDeployVO, ProjectDTO projectDTO) {
        return new DeploySourceVO()
                .setType(dockerDeployVO.getSourceType())
                .setProjectName(projectDTO.getName());
    }

    @Override
    public DevopsDockerInstanceDTO baseQuery(Long instanceId) {
        return devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);
    }

    @Override
    public DevopsDockerInstanceDTO queryByAppIdAndContainerId(Long appId, String containerId) {
        notNull(appId, ResourceCheckConstant.DEVOPS_APP_ID_IS_NULL);
        notNull(containerId, ResourceCheckConstant.DEVOPS_APP_CONTAINER_ID_IS_NULL);

        DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
        devopsDockerInstanceDTO.setAppId(appId);
        devopsDockerInstanceDTO.setContainerId(containerId);
        return devopsDockerInstanceMapper.selectOne(devopsDockerInstanceDTO);
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
        notNull(hostId, ResourceCheckConstant.DEVOPS_HOST_ID_IS_NULL);

        return devopsDockerInstanceMapper.listByHostId(hostId);
    }

    @Override
    public List<DevopsDockerInstanceDTO> listByAppId(Long appId) {
        notNull(appId, ResourceCheckConstant.DEVOPS_APP_ID_IS_NULL);

        DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
        devopsDockerInstanceDTO.setAppId(appId);

        return devopsDockerInstanceMapper.select(devopsDockerInstanceDTO);
    }

    @Override
    public DevopsDockerInstanceDTO queryByHostIdAndName(Long hostId, String containerName) {
        notNull(hostId, ResourceCheckConstant.DEVOPS_HOST_ID_IS_NULL);
        notNull(containerName, ResourceCheckConstant.DEVOPS_CONTAINER_NAME_IS_NULL);
        return devopsDockerInstanceMapper.selectOne(new DevopsDockerInstanceDTO(hostId, containerName));
    }

    @Override
    @Transactional
    public void deleteByAppId(Long appId) {
        notNull(appId, ResourceCheckConstant.DEVOPS_APP_ID_IS_NULL);

        DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
        devopsDockerInstanceDTO.setAppId(appId);

        devopsDockerInstanceMapper.delete(devopsDockerInstanceDTO);
    }

    @Override
    @Transactional
    public void baseCreate(DevopsDockerInstanceDTO devopsDockerInstanceDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsDockerInstanceMapper, devopsDockerInstanceDTO, ERROR_SAVE_DOCKER_INSTANCE_FAILED);
    }

    @Override
    public void createOrUpdate(String hostId, DockerProcessUpdatePayload processPayload) {
        Long appId = processPayload.getInstanceId();

        List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOList = devopsDockerInstanceService.listByAppId(appId);

        Map<String, DevopsDockerInstanceDTO> instanceDTOMap = devopsDockerInstanceDTOList.stream().collect(Collectors.toMap(DevopsDockerInstanceDTO::getName, Function.identity()));
        // 处理更新的数据
        List<DockerProcessInfoVO> updateProcessInfos = processPayload.getUpdateProcessInfos();
        if (!CollectionUtils.isEmpty(updateProcessInfos)) {
            updateProcessInfos.forEach(addProcessInfo -> {
                DevopsDockerInstanceDTO devopsDockerInstanceDTO = instanceDTOMap.get(addProcessInfo.getContainerName());

                if (devopsDockerInstanceDTO != null) {
                    devopsDockerInstanceDTO.setStatus(addProcessInfo.getStatus());
                    devopsDockerInstanceDTO.setName(addProcessInfo.getContainerName());
                    devopsDockerInstanceDTO.setContainerId(addProcessInfo.getContainerId());
                    devopsDockerInstanceDTO.setImage(addProcessInfo.getImage());
                    devopsDockerInstanceDTO.setPorts(addProcessInfo.getPorts());
                    devopsDockerInstanceService.baseUpdate(devopsDockerInstanceDTO);
                } else {
                    devopsDockerInstanceDTO = io.choerodon.devops.infra.util.ConvertUtils.convertObject(addProcessInfo, DevopsDockerInstanceDTO.class);
                    devopsDockerInstanceDTO.setAppId(appId);
                    devopsDockerInstanceDTO.setName(addProcessInfo.getContainerName());
                    devopsDockerInstanceDTO.setHostId(Long.valueOf(hostId));
                    devopsDockerInstanceDTO.setSourceType(AppSourceType.CUSTOM.getValue());
                    devopsDockerInstanceService.baseCreate(devopsDockerInstanceDTO);
                }
            });
        }
        Map<String, DockerProcessInfoVO> updateProcessInfoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(devopsDockerInstanceDTOList)) {
            if (!CollectionUtils.isEmpty(updateProcessInfos)) {
                updateProcessInfoMap = updateProcessInfos.stream().collect(Collectors.toMap(DockerProcessInfoVO::getContainerName, Function.identity()));
            }
            Map<String, DockerProcessInfoVO> finalUpdateProcessInfoMap = updateProcessInfoMap;
            devopsDockerInstanceDTOList.forEach(devopsDockerInstanceDTO -> {
                DockerProcessInfoVO dockerProcessInfoVO = finalUpdateProcessInfoMap.get(devopsDockerInstanceDTO.getName());
                if (dockerProcessInfoVO == null) {
                    devopsDockerInstanceService.baseDelete(devopsDockerInstanceDTO.getId());
                }
            });
        }

    }
}
