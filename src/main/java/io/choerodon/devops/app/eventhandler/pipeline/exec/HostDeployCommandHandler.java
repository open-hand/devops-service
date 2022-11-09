package io.choerodon.devops.app.eventhandler.pipeline.exec;

import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiHostDeployInfoDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.enums.HostDeployType;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.feign.operator.*;
import io.choerodon.devops.infra.handler.HostConnectionHandler;
import io.choerodon.devops.infra.mapper.*;

@Component
public class HostDeployCommandHandler extends AbstractCiCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostDeployCommandHandler.class);
    @Autowired
    protected DevopsCdAuditRecordService devopsCdAuditRecordService;

    @Autowired
    protected DevopsCdJobRecordService devopsCdJobRecordService;

    @Autowired
    protected DevopsCdPipelineRecordMapper devopsCdPipelineRecordMapper;

    @Autowired
    protected DevopsCdJobRecordMapper devopsCdJobRecordMapper;

    @Autowired
    protected DevopsCdStageRecordService devopsCdStageRecordService;

    @Autowired
    protected DevopsCdStageRecordMapper devopsCdStageRecordMapper;

    @Autowired
    protected RdupmClientOperator rdupmClientOperator;

    @Autowired
    protected BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    protected DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;

    @Autowired
    protected AppServiceMapper appServiceMapper;

    @Autowired
    protected DevopsCdAuditService devopsCdAuditService;

    @Autowired
    protected DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;

    @Autowired
    protected DevopsGitlabCommitService devopsGitlabCommitService;

    @Autowired
    protected AppServiceService applicationService;

    @Autowired
    protected CiPipelineMavenService ciPipelineMavenService;

    @Autowired
    protected TestServiceClientOperator testServiceClientoperator;

    @Autowired
    protected DevopsHostMapper devopsHostMapper;

    @Autowired
    protected DevopsDeployRecordService devopsDeployRecordService;

    @Autowired
    protected DevopsHostCommandService devopsHostCommandService;

    @Autowired
    protected KeySocketSendHelper webSocketHelper;
    @Autowired
    protected WorkFlowServiceOperator workFlowServiceOperator;
    @Autowired
    protected DevopsHostAppMapper devopsHostAppMapper;

    @Autowired
    protected DevopsHostAppService devopsHostAppService;
    @Autowired
    @Lazy
    protected DevopsCdPipelineService devopsCdPipelineService;
    @Autowired
    protected DevopsHostAppInstanceService devopsHostAppInstanceService;
    @Autowired
    protected DevopsCdJobService devopsCdJobService;
    @Autowired
    protected DevopsCdHostDeployInfoService devopsCdHostDeployInfoService;
    @Autowired
    protected DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    protected DevopsDeploymentService devopsDeploymentService;
    @Autowired
    protected AppServiceInstanceService appServiceInstanceService;
    @Autowired
    protected HostConnectionHandler hostConnectionHandler;
    @Autowired
    protected DevopsHostService devopsHostService;
    @Autowired
    protected CiPipelineImageService ciPipelineImageService;
    @Autowired
    protected DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    protected DevopsDockerInstanceMapper devopsDockerInstanceMapper;
    @Autowired
    protected DockerComposeService dockerComposeService;
    @Autowired
    protected DockerComposeValueService dockerComposeValueService;
    @Autowired
    @Lazy
    protected DevopsCiPipelineService devopsCiPipelineService;
    @Autowired
    @Lazy
    protected AppServiceService appServiceService;
    @Autowired
    protected GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    protected AppExternalConfigService appExternalConfigService;
    @Autowired
    private DevopsCiHostDeployInfoMapper devopsCiHostDeployInfoMapper;


    @Override
    public CiCommandTypeEnum getType() {
        return CiCommandTypeEnum.HOST_DEPLOY;
    }

    @Override
    protected void execute(AppServiceDTO appServiceDTO, Long gitlabPipelineId, Long gitlabJobId, Long configId, StringBuilder log, Object content) {
        DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO = devopsCiHostDeployInfoMapper.selectByPrimaryKey(configId);
        if (devopsCiHostDeployInfoDTO.getHostDeployType().equals(RdupmTypeEnum.DOCKER.value())) {
            ApplicationContextHelper
                    .getSpringFactory()
                    .getBean(DevopsCdPipelineRecordService.class)
                    .ciPipelineDeployImage(appServiceDTO.getProjectId(), gitlabPipelineId, devopsCiHostDeployInfoDTO, log);
        } else if (devopsCiHostDeployInfoDTO.getHostDeployType().equals(HostDeployType.JAR_DEPLOY.getValue())) {
            ApplicationContextHelper
                    .getSpringFactory()
                    .getBean(DevopsCdPipelineRecordService.class)
                    .ciPipelineDeployJar(appServiceDTO.getProjectId(), appServiceDTO, gitlabPipelineId, devopsCiHostDeployInfoDTO, log);
        } else if (devopsCiHostDeployInfoDTO.getHostDeployType().equals(HostDeployType.DOCKER_COMPOSE.getValue())) {
            ApplicationContextHelper
                    .getSpringFactory()
                    .getBean(DevopsCdPipelineRecordService.class)
                    .ciPipelineDeployDockerCompose(appServiceDTO.getProjectId(), appServiceDTO, gitlabPipelineId, devopsCiHostDeployInfoDTO, log);
        } else {
            ApplicationContextHelper
                    .getSpringFactory()
                    .getBean(DevopsCdPipelineRecordService.class)
                    .ciPipelineCustomDeploy(appServiceDTO.getProjectId(), gitlabPipelineId, devopsCiHostDeployInfoDTO, log);
        }
    }
}
