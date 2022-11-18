package io.choerodon.devops.app.eventhandler.pipeline.exec;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsCdPipelineRecordService;
import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiHostDeployInfoDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.enums.HostDeployType;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsCiHostDeployInfoMapper;

@Component
public class HostDeployCommandHandler extends AbstractCiCommandHandler {
    @Autowired
    private DevopsCiHostDeployInfoMapper devopsCiHostDeployInfoMapper;
    @Autowired
    private DevopsCiJobRecordService devopsCiJobRecordService;
    @Autowired
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;

    @Override
    public CiCommandTypeEnum getType() {
        return CiCommandTypeEnum.HOST_DEPLOY;
    }

    @Override
    protected void execute(AppServiceDTO appServiceDTO, Long gitlabPipelineId, Long gitlabJobId, Long configId, StringBuilder log, Map<String, Object> content) {
        DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO = devopsCiHostDeployInfoMapper.selectByPrimaryKey(configId);
        Long commandId;
        if (devopsCiHostDeployInfoDTO.getHostDeployType().equals(RdupmTypeEnum.DOCKER.value())) {
            commandId = devopsCdPipelineRecordService
                    .ciPipelineDeployImage(appServiceDTO.getProjectId(), gitlabPipelineId, devopsCiHostDeployInfoDTO, log);
        } else if (devopsCiHostDeployInfoDTO.getHostDeployType().equals(HostDeployType.JAR_DEPLOY.getValue())) {
            commandId = devopsCdPipelineRecordService
                    .ciPipelineDeployJar(appServiceDTO.getProjectId(), appServiceDTO, gitlabPipelineId, devopsCiHostDeployInfoDTO, log);
        } else if (devopsCiHostDeployInfoDTO.getHostDeployType().equals(HostDeployType.DOCKER_COMPOSE.getValue())) {
            commandId = devopsCdPipelineRecordService
                    .ciPipelineDeployDockerCompose(appServiceDTO.getProjectId(), appServiceDTO, gitlabPipelineId, devopsCiHostDeployInfoDTO, log);
        } else {
            commandId = devopsCdPipelineRecordService
                    .ciPipelineCustomDeploy(appServiceDTO.getProjectId(), gitlabPipelineId, devopsCiHostDeployInfoDTO, log);
        }
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByAppServiceIdAndGitlabJobId(appServiceDTO.getId(), gitlabJobId);
        devopsCiJobRecordDTO.setCommandId(commandId);
        devopsCiJobRecordService.baseUpdate(devopsCiJobRecordDTO);
        content.put("commandId", commandId.toString());
    }
}
