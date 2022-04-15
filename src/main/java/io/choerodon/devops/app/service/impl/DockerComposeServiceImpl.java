package io.choerodon.devops.app.service.impl;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DockerComposeDeployVO;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.DockerComposeDeployDTO;
import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;
import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsDockerInstanceMapper;
import io.choerodon.devops.infra.util.HostDeployUtil;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/7 9:56
 */
@Service
public class DockerComposeServiceImpl implements DockerComposeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerComposeServiceImpl.class);

    @Autowired
    private DockerComposeValueService dockerComposeValueService;
    @Autowired
    private DevopsHostService devopsHostService;
    @Autowired
    private DevopsHostAppService devopsHostAppService;
    @Autowired
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsDockerInstanceMapper devopsDockerInstanceMapper;


    @Override
    @Transactional
    public DevopsHostCommandDTO deployDockerComposeApp(Long projectId, DockerComposeDeployVO dockerComposeDeployVO) {
        Long hostId = dockerComposeDeployVO.getHostId();
        String appName = dockerComposeDeployVO.getAppName();
        String appCode = dockerComposeDeployVO.getAppCode();
        String runCommand = dockerComposeDeployVO.getRunCommand();

        DevopsHostDTO devopsHostDTO = devopsHostService.checkHostAvailable(hostId);

        // 创建应用
        DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                hostId,
                appName,
                appCode,
                RdupmTypeEnum.DOCKER_COMPOSE.value(),
                OperationTypeEnum.CREATE_APP.value());

        devopsHostAppService.baseCreate(devopsHostAppDTO, "error.create.app");

        Long appId = devopsHostAppDTO.getId();
        String remark = dockerComposeDeployVO.getDockerComposeValueDTO().getRemark();
        String value = dockerComposeDeployVO.getDockerComposeValueDTO().getValue();

        // 保存部署配置
        DockerComposeValueDTO dockerComposeValueDTO = new DockerComposeValueDTO(appId,
                remark,
                value);
        dockerComposeValueService.baseCreate(dockerComposeValueDTO);

        // 更新应用信息
        DevopsHostAppDTO devopsHostAppDTO1 = devopsHostAppService.baseQuery(appId);
        devopsHostAppDTO1.setEffectValueId(dockerComposeValueDTO.getId());
        devopsHostAppDTO1.setRunCommand(runCommand);
        devopsHostAppService.baseUpdate(devopsHostAppDTO1);

        // 保存操作记录
        return deployDockerComposeApp(projectId, appId, hostId, runCommand, value, null, devopsHostDTO, devopsHostAppDTO);
    }

    @Override
    @Transactional
    public DevopsHostCommandDTO updateDockerComposeApp(Long projectId, Long appId, @Nullable Long cdJobRecordId, DockerComposeDeployVO dockerComposeDeployVO) {
        // 查询应用
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(appId);

        Long hostId = devopsHostAppDTO.getHostId();
        String appName = dockerComposeDeployVO.getAppName();
        String runCommand = dockerComposeDeployVO.getRunCommand();
        String remark;
        String value;
        Long valueId = dockerComposeDeployVO.getValueId();

        DevopsHostDTO devopsHostDTO = devopsHostService.checkHostAvailable(hostId);

        if (valueId == null) {
            remark = dockerComposeDeployVO.getDockerComposeValueDTO().getRemark();
            value = dockerComposeDeployVO.getDockerComposeValueDTO().getValue();
            // 保存部署配置,如果输入了版本备注则新建记录
            if (StringUtils.isNotBlank(remark)) {
                DockerComposeValueDTO dockerComposeValueDTO = new DockerComposeValueDTO(appId,
                        remark,
                        value);
                dockerComposeValueService.baseCreate(dockerComposeValueDTO);
                devopsHostAppDTO.setEffectValueId(dockerComposeValueDTO.getId());
            } else {
                DockerComposeValueDTO dockerComposeValueDTO = dockerComposeValueService.baseQuery(devopsHostAppDTO.getEffectValueId());
                dockerComposeValueDTO.setValue(value);
                dockerComposeValueService.baseUpdate(dockerComposeValueDTO);
            }

        } else {
            DockerComposeValueDTO dockerComposeValueDTO = dockerComposeValueService.baseQuery(valueId);
            value = dockerComposeValueDTO.getValue();
        }

        // 更新应用信息
        devopsHostAppDTO.setRunCommand(runCommand);
        devopsHostAppDTO.setName(appName);
        devopsHostAppService.baseUpdate(devopsHostAppDTO);

        return deployDockerComposeApp(projectId, appId, hostId, runCommand, value, cdJobRecordId, devopsHostDTO, devopsHostAppDTO);

    }

    @Override
    public void restartDockerComposeApp(Long projectId, Long appId) {
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(appId);
        Long hostId = devopsHostAppDTO.getHostId();

        DevopsHostDTO devopsHostDTO = devopsHostService.checkHostAvailable(devopsHostAppDTO.getHostId());

        // 保存操作记录
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO(hostId,
                HostResourceType.DOCKER_COMPOSE.value(),
                appId,
                HostCommandEnum.DEPLOY_DOCKER_COMPOSE.value(),
                HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        // 保存部署记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.MANUAL,
                null,
                DeployModeEnum.HOST,
                hostId,
                devopsHostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.DOCKER_COMPOSE,
                null,
                null,
                devopsHostAppDTO.getName(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getId(),
                new DeploySourceVO(AppSourceType.CUSTOM, projectDTO.getName()));

        // 发送部署指令给aegent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO(String.valueOf(hostId),
                HostCommandEnum.RESTART_DOCKER_COMPOSE.value(),
                String.valueOf(devopsHostCommandDTO.getId()),
                JsonHelper.marshalByJackson(new DockerComposeDeployDTO(String.valueOf(hostId), String.valueOf(appId), null, null)));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>> deploy docker instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.DOCKER_COMPOSE, hostId, appId),
                JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    public Page<DevopsDockerInstanceVO> pageContainers(Long projectId, Long id, PageRequest pageable, String name, String param) {
        Page<DevopsDockerInstanceVO> pageInfo = PageHelper.doPage(pageable, () -> devopsDockerInstanceMapper.listByAppId(id, name, param));

        if (CollectionUtils.isEmpty(pageInfo.getContent())) {
            return pageInfo;
        }
        for (DevopsDockerInstanceVO devopsDockerInstanceVO : pageInfo.getContent()) {
            devopsDockerInstanceVO.setDevopsHostCommandDTO(devopsHostCommandService.queryInstanceLatest(devopsDockerInstanceVO.getId(), HostResourceType.DOCKER_PROCESS.value()));
        }
        return pageInfo;
    }

    @Override
    @Transactional
    public void deleteAppData(Long id) {
        // 1. 删除values文件
        dockerComposeValueService.deleteByAppId(id);
        // 2. 删除应用数据
        devopsHostAppService.baseDelete(id);
        // 3. 删除实例数据
        devopsDockerInstanceService.deleteByAppId(id);

    }

    @Override
    public void stopContainer(Long projectId, Long appId, Long instanceId) {
        operatorContainer(appId, instanceId, HostCommandEnum.STOP_DOCKER_IN_COMPOSE);
    }

    @Override
    public void startContainer(Long projectId, Long appId, Long instanceId) {
        operatorContainer(appId, instanceId, HostCommandEnum.START_DOCKER_IN_COMPOSE);
    }

    @Override
    public void removeContainer(Long projectId, Long appId, Long instanceId) {
        operatorContainer(appId, instanceId, HostCommandEnum.REMOVE_DOCKER_IN_COMPOSE);
    }

    @Override
    public Page<DockerComposeValueDTO> listValueRecords(Long projectId, Long id, PageRequest pageable, String searchParam) {
        return PageHelper.doPage(pageable, () -> dockerComposeValueService.listRemarkValuesByAppId(id, searchParam));
    }

    @Override
    public void restartContainer(Long projectId, Long appId, Long instanceId) {
        operatorContainer(appId, instanceId, HostCommandEnum.RESTART_DOCKER_IN_COMPOSE);
    }

    private void operatorContainer(Long appId, Long instanceId, HostCommandEnum operator) {
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(appId);
        Long hostId = devopsHostAppDTO.getHostId();

        devopsHostService.checkHostAvailable(devopsHostAppDTO.getHostId());

        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.baseQuery(instanceId);
        // 保存操作记录
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO(hostId,
                HostResourceType.DOCKER_PROCESS.value(),
                instanceId,
                operator.value(),
                HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);


        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        dockerProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        // 发送部署指令给aegent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO(String.valueOf(hostId),
                operator.value(),
                String.valueOf(devopsHostCommandDTO.getId()),
                JsonHelper.marshalByJackson(dockerProcessInfoVO));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>> deploy docker instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.DOCKER_COMPOSE, hostId, appId),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    private DevopsHostCommandDTO deployDockerComposeApp(Long projectId, Long appId, Long hostId, String runCommand, String value, @Nullable Long cdJobRecordId, DevopsHostDTO devopsHostDTO, DevopsHostAppDTO devopsHostAppDTO) {
        // 保存操作记录
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO(hostId,
                HostResourceType.DOCKER_COMPOSE.value(),
                appId,
                HostCommandEnum.DEPLOY_DOCKER_COMPOSE.value(),
                HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandDTO.setCdJobRecordId(cdJobRecordId);
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);


        // 保存部署记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.MANUAL,
                null,
                DeployModeEnum.HOST,
                hostId,
                devopsHostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.DOCKER_COMPOSE,
                null,
                null,
                devopsHostAppDTO.getName(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getId(),
                new DeploySourceVO(AppSourceType.CUSTOM, projectDTO.getName()));

        runCommand = appendCmd(appId, runCommand);

        // 发送部署指令给aegent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO(String.valueOf(hostId),
                HostCommandEnum.DEPLOY_DOCKER_COMPOSE.value(),
                String.valueOf(devopsHostCommandDTO.getId()),
                JsonHelper.marshalByJackson(new DockerComposeDeployDTO(String.valueOf(hostId), String.valueOf(appId), value, runCommand)));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>> deploy docker instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.DOCKER_COMPOSE, hostId, appId),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
        return devopsHostCommandDTO;
    }

    private String appendCmd(Long appId, String runCommand) {
        String workingDir = HostDeployUtil.getWorkingDir(appId);
        String intoWorkdir = "cd " + workingDir;
        return intoWorkdir + "\n" + runCommand;
    }
}
