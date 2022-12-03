//package io.choerodon.devops.app.service.impl;
//
//import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_YAML_FORMAT_INVALID;
//import static org.hzero.core.base.BaseConstants.Symbol.SLASH;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import com.alibaba.fastjson.JSONObject;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import net.schmizz.sshj.SSHClient;
//import net.schmizz.sshj.connection.channel.direct.Session;
//import org.apache.commons.lang.BooleanUtils;
//import org.hzero.websocket.helper.KeySocketSendHelper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.Assert;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.ObjectUtils;
//import org.yaml.snakeyaml.DumperOptions;
//import org.yaml.snakeyaml.Yaml;
//import sun.misc.BASE64Decoder;
//
//import io.choerodon.core.convertor.ApplicationContextHelper;
//import io.choerodon.core.exception.CommonException;
//import io.choerodon.core.oauth.DetailsHelper;
//import io.choerodon.devops.api.vo.*;
//import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
//import io.choerodon.devops.api.vo.deploy.JarDeployVO;
//import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
//import io.choerodon.devops.api.vo.pipeline.ExternalApprovalJobVO;
//import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
//import io.choerodon.devops.api.vo.test.ApiTestTaskRecordVO;
//import io.choerodon.devops.app.eventhandler.payload.HostDeployPayload;
//import io.choerodon.devops.app.service.*;
//import io.choerodon.devops.infra.constant.DevopsHostConstants;
//import io.choerodon.devops.infra.constant.MiscConstants;
//import io.choerodon.devops.infra.constant.PipelineCheckConstant;
//import io.choerodon.devops.infra.dto.*;
//import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
//import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
//import io.choerodon.devops.infra.dto.iam.IamUserDTO;
//import io.choerodon.devops.infra.dto.iam.ProjectDTO;
//import io.choerodon.devops.infra.dto.repo.*;
//import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
//import io.choerodon.devops.infra.dto.workflow.DevopsPipelineStageDTO;
//import io.choerodon.devops.infra.dto.workflow.DevopsPipelineTaskDTO;
//import io.choerodon.devops.infra.enums.*;
//import io.choerodon.devops.infra.enums.deploy.*;
//import io.choerodon.devops.infra.enums.host.HostCommandEnum;
//import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
//import io.choerodon.devops.infra.enums.host.HostResourceType;
//import io.choerodon.devops.infra.feign.operator.*;
//import io.choerodon.devops.infra.handler.HostConnectionHandler;
//import io.choerodon.devops.infra.mapper.*;
//import io.choerodon.devops.infra.util.*;
//
///**
// * 〈功能简述〉
// * 〈〉
// *
// * @author wanghao
// * @since 2020/7/2 10:41
// */
//@Service
//public class DevopsCdPipelineRecordServiceImpl implements DevopsCdPipelineRecordService {
//
//    protected static final String ERROR_SAVE_PIPELINE_RECORD_FAILED = "devops.save.pipeline.record.failed";
//    protected static final String DEVOPS_DEPLOY_FAILED = "devops.deploy.failed";
//    protected static final String ERROR_UPDATE_PIPELINE_RECORD_FAILED = "devops.update.pipeline.record.failed";
//    protected static final String ENV = "env";
//    protected static final String HOST = "host";
//
//    protected static final Logger LOGGER = LoggerFactory.getLogger(DevopsCdPipelineRecordServiceImpl.class);
//
//    protected static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
//    protected static final String CUSTOM_REPO = "CUSTOM_REPO";
//    protected static final String CREATE = "create";
//    protected static final BASE64Decoder decoder = new BASE64Decoder();
//
////    @Autowired
////    protected DevopsCdAuditRecordService devopsCdAuditRecordService;
//
////    @Autowired
////    protected DevopsCdJobRecordService devopsCdJobRecordService;
//
////    @Autowired
////    protected DevopsCdPipelineRecordMapper devopsCdPipelineRecordMapper;
////
////    @Autowired
////    protected DevopsCdJobRecordMapper devopsCdJobRecordMapper;
//
////    @Autowired
////    protected DevopsCdStageRecordService devopsCdStageRecordService;
//
////    @Autowired
////    protected DevopsCdStageRecordMapper devopsCdStageRecordMapper;
//
//    @Autowired
//    protected RdupmClientOperator rdupmClientOperator;
//
//    @Autowired
//    protected BaseServiceClientOperator baseServiceClientOperator;
//
//    @Autowired
//    protected DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
//
//    @Autowired
//    protected AppServiceMapper appServiceMapper;
//
//    @Autowired
//    protected DevopsCdAuditService devopsCdAuditService;
//
//    @Autowired
//    protected DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
//
//    @Autowired
//    protected DevopsGitlabCommitService devopsGitlabCommitService;
//
//    @Autowired
//    protected AppServiceService applicationService;
//
//    @Autowired
//    protected CiPipelineMavenService ciPipelineMavenService;
//
//    @Autowired
//    protected TestServiceClientOperator testServiceClientoperator;
//
//    @Autowired
//    protected DevopsHostMapper devopsHostMapper;
//
//    @Autowired
//    protected SshUtil sshUtil;
//
//    @Autowired
//    protected DevopsDeployRecordService devopsDeployRecordService;
//
//    @Autowired
//    protected DevopsHostCommandService devopsHostCommandService;
//
//    @Autowired
//    protected KeySocketSendHelper webSocketHelper;
//    @Autowired
//    protected WorkFlowServiceOperator workFlowServiceOperator;
//    @Autowired
//    protected DevopsHostAppMapper devopsHostAppMapper;
//
//    @Autowired
//    protected DevopsHostAppService devopsHostAppService;
////    @Autowired
////    @Lazy
////    protected DevopsCdPipelineService devopsCdPipelineService;
//    @Autowired
//    protected DevopsHostAppInstanceService devopsHostAppInstanceService;
//    @Autowired
//    protected DevopsCdJobService devopsCdJobService;
//    @Autowired
//    protected DevopsCdHostDeployInfoService devopsCdHostDeployInfoService;
//    @Autowired
//    protected DevopsDeployAppCenterService devopsDeployAppCenterService;
//    @Autowired
//    protected DevopsDeploymentService devopsDeploymentService;
//    @Autowired
//    protected AppServiceInstanceService appServiceInstanceService;
//    @Autowired
//    protected HostConnectionHandler hostConnectionHandler;
//    @Autowired
//    protected DevopsHostService devopsHostService;
//    @Autowired
//    protected CiPipelineImageService ciPipelineImageService;
//    @Autowired
//    protected DevopsDockerInstanceService devopsDockerInstanceService;
//    @Autowired
//    protected DevopsDockerInstanceMapper devopsDockerInstanceMapper;
//    @Autowired
//    protected DockerComposeService dockerComposeService;
//    @Autowired
//    protected DockerComposeValueService dockerComposeValueService;
//    @Autowired
//    @Lazy
//    protected DevopsCiPipelineService devopsCiPipelineService;
//    @Autowired
//    @Lazy
//    protected AppServiceService appServiceService;
//    @Autowired
//    protected GitlabServiceClientOperator gitlabServiceClientOperator;
//    @Autowired
//    protected AppExternalConfigService appExternalConfigService;
//    @Autowired
//    private DevopsCiHostDeployInfoService devopsCiHostDeployInfoService;
//
//
//    @Override
//    public DevopsCdPipelineRecordDTO queryByGitlabPipelineId(Long devopsPipelineId, Long gitlabPipelineId) {
//        Assert.notNull(gitlabPipelineId, PipelineCheckConstant.DEVOPS_GITLAB_PIPELINE_ID_IS_NULL);
//        Assert.notNull(devopsPipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);
//
//        return devopsCdPipelineRecordMapper.queryByPipelineIdAndGitlabPipelineId(devopsPipelineId, gitlabPipelineId);
//    }
//
//    @Override
//    @Transactional
//    public void save(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO) {
//        if (devopsCdPipelineRecordMapper.insertSelective(devopsCdPipelineRecordDTO) != 1) {
//            throw new CommonException(ERROR_SAVE_PIPELINE_RECORD_FAILED);
//        }
//    }
//
//    @Override
//    @Transactional
//    public void updateStatusById(Long pipelineRecordId, String status) {
//        DevopsCdPipelineRecordDTO pipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
//
//        // 已取消的流水线 不能更新为成功、失败状态
//        if (pipelineRecordDTO.getStatus().equals(PipelineStatus.CANCELED.toValue())
//                && (status.equals(PipelineStatus.FAILED.toValue())
//                || status.equals(PipelineStatus.SUCCESS.toValue()))) {
//            LOGGER.info("cancel pipeline can not update status!! pipeline record Id {}", pipelineRecordDTO.getId());
//            return;
//        }
//
//        pipelineRecordDTO.setStatus(status);
//        if (devopsCdPipelineRecordMapper.updateByPrimaryKey(pipelineRecordDTO) != 1) {
//            throw new CommonException(ERROR_UPDATE_PIPELINE_RECORD_FAILED);
//        }
//    }
//
//    /**
//     * 准备workflow创建实例所需数据
//     * 为此workflow下所有stage创建记录
//     */
//    @Override
//    public DevopsPipelineDTO createCDWorkFlowDTO(Long pipelineRecordId, Boolean isRetry) {
//        // 1.
//        DevopsPipelineDTO devopsPipelineDTO = new DevopsPipelineDTO();
//        devopsPipelineDTO.setPipelineRecordId(pipelineRecordId);
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
//        devopsPipelineDTO.setPipelineName(devopsCdPipelineRecordDTO.getPipelineName());
//        devopsPipelineDTO.setBusinessKey(devopsCdPipelineRecordDTO.getBusinessKey());
//        devopsPipelineDTO.setPipelineId(devopsCdPipelineRecordDTO.getPipelineId());
//
//        // 2.
//        List<DevopsPipelineStageDTO> devopsPipelineStageDTOS = new ArrayList<>();
//        List<DevopsCdStageRecordDTO> stageRecordDTOList;
//        if (BooleanUtils.isTrue(isRetry)) {
//            stageRecordDTOList = devopsCdStageRecordMapper.queryRetryStage(pipelineRecordId);
//        } else {
//            stageRecordDTOList = devopsCdStageRecordService.queryByPipelineRecordId(pipelineRecordId);
//        }
//
//        if (CollectionUtils.isEmpty(stageRecordDTOList)) {
//            return null;
//        }
//        stageRecordDTOList = stageRecordDTOList.stream().sorted(Comparator.comparing(DevopsCdStageRecordDTO::getSequence)).collect(Collectors.toList());
//
//        for (int i = 0; i < stageRecordDTOList.size(); i++) {
//            // 3.
//            DevopsPipelineStageDTO stageDTO = new DevopsPipelineStageDTO();
//            DevopsCdStageRecordDTO stageRecordDTO = stageRecordDTOList.get(i);
//            stageDTO.setStageRecordId(stageRecordDTO.getId());
//            // 4.
//            List<DevopsCdJobRecordDTO> jobRecordDTOList;
//            if (Boolean.FALSE.equals(isRetry) || i > 0) {
//                jobRecordDTOList = devopsCdJobRecordService.queryByStageRecordId(stageRecordDTO.getId());
//            } else {
//                jobRecordDTOList = devopsCdJobRecordMapper.queryRetryJob(stageRecordDTO.getId());
//            }
//            jobRecordDTOList = jobRecordDTOList.stream().sorted(Comparator.comparing(DevopsCdJobRecordDTO::getSequence)).collect(Collectors.toList());
//            List<DevopsPipelineTaskDTO> taskDTOList = new ArrayList<>();
//            if (!CollectionUtils.isEmpty(jobRecordDTOList)) {
//                jobRecordDTOList.forEach(jobRecordDTO -> {
//                    DevopsPipelineTaskDTO taskDTO = new DevopsPipelineTaskDTO();
//                    taskDTO.setTaskRecordId(jobRecordDTO.getId());
//                    taskDTO.setTaskName(jobRecordDTO.getName());
//                    if (jobRecordDTO.getType().equals(JobTypeEnum.CD_AUDIT.value())) {
//                        List<DevopsCdAuditRecordDTO> jobAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(jobRecordDTO.getId());
//                        if (CollectionUtils.isEmpty(jobAuditRecordDTOS)) {
//                            throw new CommonException("devops.audit.job.noUser");
//                        }
//                        List<String> taskUsers = jobAuditRecordDTOS.stream().map(t -> TypeUtil.objToString(t.getUserId())).collect(Collectors.toList());
//                        taskDTO.setUsernames(taskUsers);
//                        taskDTO.setMultiAssign(taskUsers.size() > 1);
//                    } else if (jobRecordDTO.getType().equals(JobTypeEnum.CD_API_TEST.value())) {
//                        CdApiTestConfigVO cdApiTestConfigVO = JsonHelper.unmarshalByJackson(jobRecordDTO.getMetadata(), CdApiTestConfigVO.class);
//                        taskDTO.setDeployJobName(cdApiTestConfigVO.getDeployJobName());
//                    }
//                    taskDTO.setTaskType(jobRecordDTO.getType());
//                    if (jobRecordDTO.getCountersigned() != null) {
//                        taskDTO.setSign(jobRecordDTO.getCountersigned().longValue());
//                    }
//                    taskDTOList.add(taskDTO);
//                });
//            }
//            stageDTO.setTasks(taskDTOList);
//            // 5. 审核任务处理
//            // 在workflow 是先渲染阶段 在渲染阶段任务
//            if (i != stageRecordDTOList.size() - 1) {
//                stageDTO.setNextStageTriggerType(TriggerTypeEnum.AUTO.value());
//            }
//            devopsPipelineStageDTOS.add(stageDTO);
//        }
//        devopsPipelineDTO.setStages(devopsPipelineStageDTOS);
//        return devopsPipelineDTO;
//    }
//
//    @Override
//    public DevopsPipelineDTO createCDWorkFlowDTO(Long pipelineRecordId) {
//        return createCDWorkFlowDTO(pipelineRecordId, false);
//    }
//
//    protected String getMavenVersion(String version) {
//        if (version.contains(SLASH)) {
//            return version.split(SLASH)[0];
//        } else {
//            return version;
//        }
//    }
//
//    private void closeSsh(SSHClient ssh, Session session) {
//        try {
//            if (session != null) {
//                session.close();
//            }
//            ssh.disconnect();
//        } catch (IOException e) {
//            LOGGER.error("error close ssh", e);
//        }
//    }
//
//    @Override
//    @Transactional
//    public void cdHostDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
//        HostDeployPayload hostDeployPayload = new HostDeployPayload(pipelineRecordId, cdStageRecordId, cdJobRecordId);
//        DevopsCdPipelineRecordDTO pipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
//        StringBuilder log = new StringBuilder();
//        if (PipelineStatus.CANCELED.toValue().equals(pipelineRecordDTO.getStatus())) {
//            return;
//        }
//        CustomContextUtil.setUserContext(pipelineRecordDTO.getCreatedBy());
//
//        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(hostDeployPayload.getJobRecordId());
//        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
//        try {
//            if (cdHostDeployConfigVO.getHostDeployType().equals(RdupmTypeEnum.DOCKER.value())) {
//                ApplicationContextHelper
//                        .getSpringFactory()
//                        .getBean(DevopsCdPipelineRecordService.class)
//                        .pipelineDeployImage(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId(), log);
//            } else if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.JAR_DEPLOY.getValue())) {
//                ApplicationContextHelper
//                        .getSpringFactory()
//                        .getBean(DevopsCdPipelineRecordService.class)
//                        .pipelineDeployJar(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId(), log);
//            } else if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.DOCKER_COMPOSE.getValue())) {
//                ApplicationContextHelper
//                        .getSpringFactory()
//                        .getBean(DevopsCdPipelineRecordService.class)
//                        .pipelineDeployDockerCompose(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId(), log);
//            } else {
//                ApplicationContextHelper
//                        .getSpringFactory()
//                        .getBean(DevopsCdPipelineRecordService.class)
//                        .pipelineCustomDeploy(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId(), log);
//            }
//        } catch (Exception e) {
//            LOGGER.error(" deploy failed!, error msg is", e);
//            log.append("Deploy app instance failed").append(System.lineSeparator());
//            log.append(LogUtil.cutOutString(LogUtil.readContentOfThrowable(e), 2500)).append(System.lineSeparator());
//            devopsCdJobRecordService.updateJobStatusFailed(cdJobRecordId, log.toString());
//            devopsCdStageRecordService.updateStageStatusFailed(cdStageRecordId);
//            updatePipelineStatusFailed(pipelineRecordId);
//            workFlowServiceOperator.stopInstance(pipelineRecordDTO.getProjectId(), pipelineRecordDTO.getBusinessKey());
//        }
//
//
//    }
//
//    @Override
//    @Async
//    @Transactional
//    public void cdHostDeployAsync(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
//        cdHostDeploy(pipelineRecordId, cdStageRecordId, cdJobRecordId);
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void pipelineCustomDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId, StringBuilder log) {
//        LOGGER.info("start jar deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
//
//        DevopsCdPipelineRecordDTO cdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
//        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordService.queryById(cdJobRecordId);
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(cdPipelineRecordDTO.getProjectId());
//        Long projectId = projectDTO.getId();
//
//        DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO = devopsCdHostDeployInfoService.queryById(jobRecordDTO.getDeployInfoId());
//
//        Long hostId = devopsCdHostDeployInfoDTO.getHostId();
//        List<Long> updatedClusterList = hostConnectionHandler.getUpdatedHostList();
//
//        if (Boolean.FALSE.equals(updatedClusterList.contains(hostId))) {
//            LOGGER.info("host {} not connect, skip this task.", hostId);
//            updateStatusToSkip(cdPipelineRecordDTO, jobRecordDTO);
//            return;
//        }
//
//        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
//
//        DevopsHostAppDTO devopsHostAppDTO;
//        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO;
//        if (DeployTypeEnum.CREATE.value().equals(devopsCdHostDeployInfoDTO.getDeployType())) {
//            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
//                    hostId,
//                    devopsCdHostDeployInfoDTO.getAppName(),
//                    devopsCdHostDeployInfoDTO.getAppCode(),
//                    RdupmTypeEnum.OTHER.value(),
//                    OperationTypeEnum.PIPELINE_DEPLOY.value());
//            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);
//            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
//                    hostId,
//                    devopsHostAppDTO.getId(),
//                    devopsCdHostDeployInfoDTO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
//                    null,
//                    null,
//                    devopsCdHostDeployInfoDTO.getPreCommand(),
//                    devopsCdHostDeployInfoDTO.getRunCommand(),
//                    devopsCdHostDeployInfoDTO.getPostCommand(),
//                    devopsCdHostDeployInfoDTO.getKillCommand(),
//                    devopsCdHostDeployInfoDTO.getHealthProb());
//
//            devopsCdHostDeployInfoDTO.setAppId(devopsHostAppDTO.getId());
//            devopsCdHostDeployInfoDTO.setDeployType(DeployTypeEnum.UPDATE.value());
//            devopsCdHostDeployInfoService.baseUpdate(devopsCdHostDeployInfoDTO);
//
//            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);
//        } else {
//            devopsHostAppDTO = devopsHostAppService.baseQuery(devopsCdHostDeployInfoDTO.getAppId());
//            devopsHostAppDTO.setName(devopsCdHostDeployInfoDTO.getAppName());
//            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);
//
//            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
//            devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
//
//            devopsHostAppInstanceDTO.setPreCommand(devopsCdHostDeployInfoDTO.getPreCommand());
//            devopsHostAppInstanceDTO.setRunCommand(devopsCdHostDeployInfoDTO.getRunCommand());
//            devopsHostAppInstanceDTO.setPostCommand(devopsCdHostDeployInfoDTO.getPostCommand());
//            devopsHostAppInstanceDTO.setKillCommand(devopsCdHostDeployInfoDTO.getKillCommand());
//            devopsHostAppInstanceDTO.setHealthProb(devopsCdHostDeployInfoDTO.getHealthProb());
//            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
//        }
//
//        Map<String, String> params = new HashMap<>();
//        String workDir = HostDeployUtil.getWorkingDir(devopsHostAppInstanceDTO.getId());
//        params.put("{{ WORK_DIR }}", workDir);
//        params.put("{{ APP_FILE_NAME }}", "");
//        params.put("{{ APP_FILE }}", "");
//
//        InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions(
//                devopsCdHostDeployInfoDTO.getAppCode(),
//                devopsHostAppInstanceDTO.getId().toString(),
//                null,
//                ObjectUtils.isEmpty(devopsCdHostDeployInfoDTO.getPreCommand()) ? "" : HostDeployUtil.getCommand(params, devopsCdHostDeployInfoDTO.getPreCommand()),
//                ObjectUtils.isEmpty(devopsCdHostDeployInfoDTO.getRunCommand()) ? "" : HostDeployUtil.getCommand(params, devopsCdHostDeployInfoDTO.getRunCommand()),
//                ObjectUtils.isEmpty(devopsCdHostDeployInfoDTO.getPostCommand()) ? "" : HostDeployUtil.getCommand(params, devopsCdHostDeployInfoDTO.getPostCommand()),
//                ObjectUtils.isEmpty(devopsCdHostDeployInfoDTO.getKillCommand()) ? "" : HostDeployUtil.getCommand(params, devopsCdHostDeployInfoDTO.getKillCommand()),
//                ObjectUtils.isEmpty(devopsCdHostDeployInfoDTO.getHealthProb()) ? "" : HostDeployUtil.getCommand(params, devopsCdHostDeployInfoDTO.getHealthProb()),
//                devopsCdHostDeployInfoDTO.getDeployType());
//
//        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
//        devopsHostCommandDTO.setCommandType(HostCommandEnum.OPERATE_INSTANCE.value());
//        devopsHostCommandDTO.setHostId(hostId);
//        devopsHostCommandDTO.setCdJobRecordId(cdJobRecordId);
//        devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
//        devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
//        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
//        devopsHostCommandService.baseCreate(devopsHostCommandDTO);
//
//        // 更新流水线状态 记录信息
//        jobRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
//        jobRecordDTO.setStartedDate(new Date());
//        jobRecordDTO.setCommandId(devopsHostCommandDTO.getId());
//        devopsCdJobRecordService.update(jobRecordDTO);
//
//        // 保存执行记录
//        devopsDeployRecordService.saveRecord(
//                jobRecordDTO.getProjectId(),
//                DeployType.AUTO,
//                devopsHostCommandDTO.getId(),
//                DeployModeEnum.HOST,
//                hostId,
//                devopsHostDTO != null ? devopsHostDTO.getName() : null,
//                PipelineStatus.SUCCESS.toValue(),
//                DeployObjectTypeEnum.OTHER,
//                devopsCdHostDeployInfoDTO.getAppName(),
//                null,
//                devopsCdHostDeployInfoDTO.getAppName(),
//                devopsCdHostDeployInfoDTO.getAppCode(),
//                devopsHostAppDTO.getId(),
//                new DeploySourceVO(AppSourceType.CURRENT_PROJECT, projectDTO.getName()));
//
//        // 3. 发送部署指令给agent
//        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
//        hostAgentMsgVO.setHostId(String.valueOf(hostId));
//        hostAgentMsgVO.setType(HostCommandEnum.OPERATE_INSTANCE.value());
//        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
//        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));
//
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
//        }
//
//        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
//                String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppDTO.getId()),
//                JsonHelper.marshalByJackson(hostAgentMsgVO));
//
//
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void pipelineDeployJar(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId, StringBuilder log) {
//        LOGGER.info("start jar deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
//
//        DevopsCdPipelineRecordDTO cdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
//        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordService.queryById(cdJobRecordId);
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(cdPipelineRecordDTO.getProjectId());
//        Long projectId = projectDTO.getId();
//
//        DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO = devopsCdHostDeployInfoService.queryById(jobRecordDTO.getDeployInfoId());
//
//        Long hostId = devopsCdHostDeployInfoDTO.getHostId();
//
//        List<Long> updatedClusterList = hostConnectionHandler.getUpdatedHostList();
//
//        if (Boolean.FALSE.equals(updatedClusterList.contains(hostId))) {
//            LOGGER.info("host {} not connect, skip this task.", hostId);
//            updateStatusToSkip(cdPipelineRecordDTO, jobRecordDTO);
//            return;
//        }
//
//        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
//        CdHostDeployConfigVO.JarDeploy jarDeploy = JsonHelper.unmarshalByJackson(devopsCdHostDeployInfoDTO.getDeployJson(), CdHostDeployConfigVO.JarDeploy.class);
//
//        // 0.1 从制品库获取仓库信息
//        Long nexusRepoId;
//        String groupId;
//        String artifactId;
//        String versionRegular;
//        String version = null;
//        String downloadUrl = null;
//        String username = null;
//        String password = null;
//        if (jarDeploy.getDeploySource().equals(HostDeploySource.MATCH_DEPLOY.getValue())) {
//            nexusRepoId = jarDeploy.getRepositoryId();
//            groupId = jarDeploy.getGroupId();
//            artifactId = jarDeploy.getArtifactId();
//            versionRegular = jarDeploy.getVersionRegular();
//        } else {
//            CiCdPipelineDTO ciCdPipelineDTO = devopsCiCdPipelineMapper.selectByPrimaryKey(cdPipelineRecordDTO.getPipelineId());
//
//            CiPipelineMavenDTO ciPipelineMavenDTO = ciPipelineMavenService.queryByGitlabPipelineId(ciCdPipelineDTO.getAppServiceId(),
//                    cdPipelineRecordDTO.getGitlabPipelineId(),
//                    jarDeploy.getPipelineTask());
//            if (LOGGER.isInfoEnabled()) {
//                LOGGER.info("pipeline deploy jar, ciPipelineMavenDTO is {}", JsonHelper.marshalByJackson(ciPipelineMavenDTO));
//            }
//            nexusRepoId = ciPipelineMavenDTO.getNexusRepoId();
//            groupId = ciPipelineMavenDTO.getGroupId();
//            artifactId = ciPipelineMavenDTO.getArtifactId();
//            //0.0.1-SNAPSHOT/springbbot-0.0.1-20210506.081037-4
//            versionRegular = "^" + getMavenVersion(ciPipelineMavenDTO.getVersion()) + "$";
//            if (nexusRepoId == null) {
//                downloadUrl = ciPipelineMavenDTO.calculateDownloadUrl();
//                username = DESEncryptUtil.decode(ciPipelineMavenDTO.getUsername());
//                password = DESEncryptUtil.decode(ciPipelineMavenDTO.getPassword());
//                version = ciPipelineMavenDTO.getVersion();
//            }
//        }
//        JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO(username, password, downloadUrl);
//        JarDeployVO jarDeployVO = null;
//        if (nexusRepoId != null) {
//            // 0.3 获取并记录信息
//            List<C7nNexusComponentDTO> nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), cdPipelineRecordDTO.getProjectId(), nexusRepoId, groupId, artifactId, versionRegular);
//            if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
//                LOGGER.info("no jar to deploy,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
//                updateStatusToSkip(cdPipelineRecordDTO, jobRecordDTO);
//                return;
//            }
//            List<NexusMavenRepoDTO> mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), cdPipelineRecordDTO.getProjectId(), Collections.singleton(nexusRepoId));
//            if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
//                throw new CommonException("devops.get.maven.config");
//            }
//
//            C7nNexusComponentDTO c7nNexusComponentDTO = nexusComponentDTOList.get(0);
//            C7nNexusRepoDTO c7nNexusRepoDTO = rdupmClientOperator.getMavenRepo(projectDTO.getOrganizationId(), projectId, nexusRepoId);
//
//            ProdJarInfoVO prodJarInfoVO = new ProdJarInfoVO(c7nNexusRepoDTO.getConfigId(),
//                    nexusRepoId,
//                    groupId,
//                    artifactId,
//                    c7nNexusComponentDTO.getVersion());
//            downloadUrl = nexusComponentDTOList.get(0).getDownloadUrl();
//            username = mavenRepoDTOList.get(0).getNePullUserId();
//            password = mavenRepoDTOList.get(0).getNePullUserPassword();
//            version = c7nNexusComponentDTO.getVersion();
//
//            jarDeployVO = new JarDeployVO(AppSourceType.CURRENT_PROJECT.getValue(),
//                    devopsCdHostDeployInfoDTO.getAppName(),
//                    devopsCdHostDeployInfoDTO.getAppCode(),
//                    devopsCdHostDeployInfoDTO.getPreCommand(),
//                    devopsCdHostDeployInfoDTO.getRunCommand(),
//                    devopsCdHostDeployInfoDTO.getPostCommand(),
//                    devopsCdHostDeployInfoDTO.getKillCommand(),
//                    devopsCdHostDeployInfoDTO.getHealthProb(),
//                    prodJarInfoVO,
//                    devopsCdHostDeployInfoDTO.getDeployType());
//        } else {
//            jarDeployVO = new JarDeployVO(AppSourceType.CUSTOM_JAR.getValue(),
//                    devopsCdHostDeployInfoDTO.getAppName(),
//                    devopsCdHostDeployInfoDTO.getAppCode(),
//                    devopsCdHostDeployInfoDTO.getPreCommand(),
//                    devopsCdHostDeployInfoDTO.getRunCommand(),
//                    devopsCdHostDeployInfoDTO.getPostCommand(),
//                    devopsCdHostDeployInfoDTO.getKillCommand(),
//                    devopsCdHostDeployInfoDTO.getHealthProb(),
//                    jarPullInfoDTO,
//                    devopsCdHostDeployInfoDTO.getDeployType());
//        }
//
//        // 2.保存记录
//        DevopsCdJobDTO devopsCdJobDTO = devopsCdJobService.queryById(jobRecordDTO.getJobId());
//        DevopsHostAppDTO devopsHostAppDTO;
//        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO;
//        if (DeployTypeEnum.CREATE.value().equals(devopsCdHostDeployInfoDTO.getDeployType())) {
//            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
//                    hostId,
//                    jarDeployVO.getAppName(),
//                    jarDeployVO.getAppCode(),
//                    RdupmTypeEnum.JAR.value(),
//                    OperationTypeEnum.PIPELINE_DEPLOY.value());
//            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);
//            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
//                    hostId,
//                    devopsHostAppDTO.getId(),
//                    jarDeployVO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
//                    jarDeployVO.getSourceType(),
//                    devopsHostAppService.calculateSourceConfig(jarDeployVO),
//                    jarDeployVO.getPreCommand(),
//                    jarDeployVO.getRunCommand(),
//                    jarDeployVO.getPostCommand(),
//                    jarDeployVO.getKillCommand(),
//                    jarDeployVO.getHealthProb());
//            devopsHostAppInstanceDTO.setGroupId(groupId);
//            devopsHostAppInstanceDTO.setArtifactId(artifactId);
//            devopsHostAppInstanceDTO.setVersion(version);
//
//            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);
//
//            // 保存appId
//            devopsCdHostDeployInfoDTO.setAppId(devopsHostAppDTO.getId());
//            devopsCdHostDeployInfoDTO.setDeployType(DeployTypeEnum.UPDATE.value());
//            devopsCdHostDeployInfoService.baseUpdate(devopsCdHostDeployInfoDTO);
//            devopsCdJobService.baseUpdate(devopsCdJobDTO);
//
//        } else {
//            devopsHostAppDTO = devopsHostAppService.baseQuery(devopsCdHostDeployInfoDTO.getAppId());
//            if (devopsHostAppDTO == null) {
//                LOGGER.info("App not found, is deleted? Skip this task.appId:{},appName:{},appCode{}", devopsCdHostDeployInfoDTO.getAppId(), devopsCdHostDeployInfoDTO.getAppName(), devopsCdHostDeployInfoDTO.getAppCode());
//                updateStatusToSkip(cdPipelineRecordDTO, jobRecordDTO);
//                return;
//            }
//            devopsHostAppDTO.setName(jarDeployVO.getAppName());
//            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);
//
//            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
//            devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
//
//            devopsHostAppInstanceDTO.setPreCommand(jarDeployVO.getPreCommand());
//            devopsHostAppInstanceDTO.setRunCommand(jarDeployVO.getRunCommand());
//            devopsHostAppInstanceDTO.setPostCommand(jarDeployVO.getPostCommand());
//            devopsHostAppInstanceDTO.setKillCommand(jarDeployVO.getKillCommand());
//            devopsHostAppInstanceDTO.setHealthProb(jarDeployVO.getHealthProb());
//            devopsHostAppInstanceDTO.setSourceType(jarDeployVO.getSourceType());
//            devopsHostAppInstanceDTO.setSourceConfig(devopsHostAppService.calculateSourceConfig(jarDeployVO));
//            devopsHostAppInstanceDTO.setVersion(version);
//            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
//        }
//
//        Map<String, String> params = new HashMap<>();
//        String workDir = HostDeployUtil.getWorkingDir(devopsHostAppInstanceDTO.getId());
//        String appFile = workDir + SLASH + artifactId;
//        params.put("{{ WORK_DIR }}", workDir);
//        params.put("{{ APP_FILE_NAME }}", artifactId);
//        params.put("{{ APP_FILE }}", appFile);
//
//        InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions(
//                jarDeployVO.getAppCode(),
//                devopsHostAppInstanceDTO.getId().toString(),
//                HostDeployUtil.getDownloadCommand(username,
//                        password,
//                        downloadUrl,
//                        appFile),
//                ObjectUtils.isEmpty(jarDeployVO.getPreCommand()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getPreCommand()),
//                ObjectUtils.isEmpty(jarDeployVO.getRunCommand()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getRunCommand()),
//                ObjectUtils.isEmpty(jarDeployVO.getPostCommand()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getPostCommand()),
//                ObjectUtils.isEmpty(jarDeployVO.getKillCommand()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getKillCommand()),
//                ObjectUtils.isEmpty(jarDeployVO.getHealthProb()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getHealthProb()),
//                jarDeployVO.getOperation());
//        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
//        devopsHostCommandDTO.setCommandType(HostCommandEnum.OPERATE_INSTANCE.value());
//        devopsHostCommandDTO.setHostId(hostId);
//        devopsHostCommandDTO.setCdJobRecordId(cdJobRecordId);
//        devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
//        devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
//        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
//        devopsHostCommandService.baseCreate(devopsHostCommandDTO);
//
//        // 更新流水线状态 记录信息
//        jobRecordDTO.setDeployMetadata(gson.toJson(jarPullInfoDTO));
//        jobRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
//        jobRecordDTO.setStartedDate(new Date());
//        jobRecordDTO.setCommandId(devopsHostCommandDTO.getId());
//        devopsCdJobRecordService.update(jobRecordDTO);
//
//        // 保存执行记录
//        devopsDeployRecordService.saveRecord(
//                jobRecordDTO.getProjectId(),
//                DeployType.AUTO,
//                devopsHostCommandDTO.getId(),
//                DeployModeEnum.HOST,
//                hostId,
//                devopsHostDTO != null ? devopsHostDTO.getName() : null,
//                PipelineStatus.SUCCESS.toValue(),
//                DeployObjectTypeEnum.JAR,
//                artifactId,
//                version,
//                devopsCdHostDeployInfoDTO.getAppName(),
//                devopsCdHostDeployInfoDTO.getAppCode(),
//                devopsHostAppDTO.getId(),
//                new DeploySourceVO(AppSourceType.CURRENT_PROJECT, projectDTO.getName()));
//
//        // 3. 发送部署指令给agent
//        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
//        hostAgentMsgVO.setHostId(String.valueOf(hostId));
//        hostAgentMsgVO.setType(HostCommandEnum.OPERATE_INSTANCE.value());
//        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
//        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));
//
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
//        }
//        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
//                String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppDTO.getId()),
//                JsonHelper.marshalByJackson(hostAgentMsgVO));
//
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void pipelineDeployImage(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId, StringBuilder log) {
//        LOGGER.info("start image deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
//        log.append("Start pipeline auto deploy task.").append(System.lineSeparator());
//        String deployVersion = null;
//        String deployObjectName = null;
//        String image = null;
//        Long appServiceId = null;
//        String repoName = null;
//        Long repoId = null;
//        String userName = null;
//        String password = null;
//        String repoType = null;
//        String tag = null;
//
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(cdJobRecordId);
//
//        DockerDeployDTO dockerDeployDTO = new DockerDeployDTO();
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsCdPipelineRecordDTO.getProjectId());
//        Long projectId = projectDTO.getId();
//
//        DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO = devopsCdHostDeployInfoService.queryById(devopsCdJobRecordDTO.getDeployInfoId());
//        CdHostDeployConfigVO.ImageDeploy imageDeploy = JsonHelper.unmarshalByJackson(devopsCdHostDeployInfoDTO.getDeployJson(), CdHostDeployConfigVO.ImageDeploy.class);
//
//        Long hostId = devopsCdHostDeployInfoDTO.getHostId();
//        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);
//        log.append("Start deploy image to host: ").append(devopsHostDTO.getName()).append(System.lineSeparator());
//
//        if (ObjectUtils.isEmpty(devopsCdPipelineRecordDTO.getGitlabPipelineId())) {
//            throw new CommonException("devops.no.gitlab.pipeline.id");
//        }
//        CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageService.queryByGitlabPipelineId(appServiceId, devopsCdPipelineRecordDTO.getGitlabPipelineId(), imageDeploy.getPipelineTask());
//        if (ciPipelineImageDTO == null) {
//            throw new CommonException("devops.deploy.images.not.exist");
//        }
//        HarborRepoDTO harborRepoDTO = rdupmClientOperator.queryHarborRepoConfigById(devopsCdPipelineRecordDTO.getProjectId(), ciPipelineImageDTO.getHarborRepoId(), ciPipelineImageDTO.getRepoType());
//
//        // 设置拉取账户
//        if (ciPipelineImageDTO.getRepoType().equals(CUSTOM_REPO)) {
//            dockerDeployDTO.setDockerPullAccountDTO(new DockerPullAccountDTO(
//                    harborRepoDTO.getHarborRepoConfig().getRepoUrl(),
//                    harborRepoDTO.getHarborRepoConfig().getLoginName(),
//                    harborRepoDTO.getHarborRepoConfig().getPassword()));
//            userName = harborRepoDTO.getHarborRepoConfig().getLoginName();
//            password = harborRepoDTO.getHarborRepoConfig().getPassword();
//            repoType = harborRepoDTO.getRepoType();
//            repoId = harborRepoDTO.getHarborRepoConfig().getRepoId();
//        } else {
//            dockerDeployDTO.setDockerPullAccountDTO(new DockerPullAccountDTO(
//                    harborRepoDTO.getHarborRepoConfig().getRepoUrl(),
//                    harborRepoDTO.getPullRobot().getName(),
//                    harborRepoDTO.getPullRobot().getToken()));
//            repoId = harborRepoDTO.getHarborRepoConfig().getRepoId();
//            repoName = harborRepoDTO.getHarborRepoConfig().getRepoName();
//            repoType = harborRepoDTO.getRepoType();
//        }
//
//        // 添加应用服务名用于部署记录  iamgeTag:172.23.xx.xx:30003/dev-25-test-25-4/go:2021.5.17-155211-master
//        String imageTag = ciPipelineImageDTO.getImageTag();
//        int indexOf = imageTag.lastIndexOf(":");
//        String imageVersion = imageTag.substring(indexOf + 1);
//        String repoImageName = imageTag.substring(0, indexOf);
//        tag = imageVersion;
//        image = ciPipelineImageDTO.getImageTag();
//        deployVersion = imageVersion;
//        deployObjectName = repoImageName.substring(repoImageName.lastIndexOf("/") + 1);
//
//        log.append("Deploy image is: ").append(imageTag).append(System.lineSeparator());
//        log.append("Container name is: ").append(imageDeploy.getContainerName()).append(System.lineSeparator());
//
//        // 1. 更新状态 记录镜像信息
//        DevopsHostAppDTO devopsHostAppDTO = getDevopsHostAppDTO(projectId, hostId, devopsCdHostDeployInfoDTO.getDeployType(), devopsCdHostDeployInfoDTO.getAppName(), devopsCdHostDeployInfoDTO.getAppCode());
//        // 2.保存记录
//        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.queryByHostIdAndName(hostId, imageDeploy.getContainerName());
//        if (devopsDockerInstanceDTO == null) {
//            // 新建实例
//            devopsDockerInstanceDTO = new DevopsDockerInstanceDTO(hostId,
//                    imageDeploy.getContainerName(),
//                    image,
//                    DockerInstanceStatusEnum.OPERATING.value(),
//                    AppSourceType.CURRENT_PROJECT.getValue(), null);
//            devopsDockerInstanceDTO.setDockerCommand(devopsCdHostDeployInfoDTO.getDockerCommand());
//            devopsDockerInstanceDTO.setRepoId(repoId);
//            devopsDockerInstanceDTO.setRepoName(repoName);
//            devopsDockerInstanceDTO.setAppId(devopsHostAppDTO.getId());
//            devopsDockerInstanceDTO.setImageName(deployObjectName);
//            devopsDockerInstanceDTO.setPassWord(password);
//            devopsDockerInstanceDTO.setUserName(userName);
//            devopsDockerInstanceDTO.setRepoType(repoType);
//            devopsDockerInstanceDTO.setTag(tag);
//            MapperUtil.resultJudgedInsertSelective(devopsDockerInstanceMapper, devopsDockerInstanceDTO, DevopsHostConstants.ERROR_SAVE_DOCKER_INSTANCE_FAILED);
//            // 保存应用实例关系
//            // 保存appId
//            devopsCdHostDeployInfoDTO.setAppId(devopsHostAppDTO.getId());
//            devopsCdHostDeployInfoDTO.setDeployType(DeployTypeEnum.UPDATE.value());
//            devopsCdHostDeployInfoService.baseUpdate(devopsCdHostDeployInfoDTO);
//        } else {
//            dockerDeployDTO.setContainerId(devopsDockerInstanceDTO.getContainerId());
//        }
//
//        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
//        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_DOCKER.value());
//        devopsHostCommandDTO.setHostId(hostId);
//        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
//        devopsHostCommandDTO.setCdJobRecordId(cdJobRecordId);
//        devopsHostCommandDTO.setInstanceId(devopsDockerInstanceDTO.getId());
//        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
//        devopsHostCommandService.baseCreate(devopsHostCommandDTO);
//
//        dockerDeployDTO.setImage(image);
//        dockerDeployDTO.setContainerName(imageDeploy.getContainerName());
//        try {
//            dockerDeployDTO.setCmd(HostDeployUtil.getDockerRunCmd(dockerDeployDTO,
//                    new String(decoder.decodeBuffer(devopsCdHostDeployInfoDTO.getDockerCommand()), StandardCharsets.UTF_8)));
//        } catch (IOException e) {
//            throw new CommonException(e);
//        }
//        dockerDeployDTO.setInstanceId(String.valueOf(devopsDockerInstanceDTO.getId()));
//
//        // 3. 保存部署记录
//        devopsDeployRecordService.saveRecord(
//                devopsCdPipelineRecordDTO.getProjectId(),
//                DeployType.AUTO,
//                devopsHostCommandDTO.getId(),
//                DeployModeEnum.HOST,
//                devopsHostDTO.getId(),
//                devopsHostDTO.getName(),
//                PipelineStatus.SUCCESS.toValue(),
//                DeployObjectTypeEnum.DOCKER,
//                deployObjectName,
//                deployVersion,
//                devopsHostAppDTO.getName(),
//                devopsHostAppDTO.getCode(),
//                devopsHostAppDTO.getId(),
//                new DeploySourceVO(AppSourceType.CURRENT_PROJECT, projectDTO.getName()));
//
//        // 4. 发送部署指令给agent
//        log.append("Sending deploy command to agent.").append(System.lineSeparator());
//        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
//        hostAgentMsgVO.setHostId(String.valueOf(hostId));
//        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_DOCKER.value());
//        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
//        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerDeployDTO));
//
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info(">>>>>>>>>>>>>>>>>>>> deploy docker instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
//        }
//
//        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
//                String.format(DevopsHostConstants.DOCKER_INSTANCE, hostId, devopsDockerInstanceDTO.getId()),
//                JsonHelper.marshalByJackson(hostAgentMsgVO));
//
//        log.append("Sending deploy command to agent success.").append(System.lineSeparator());
//
//        devopsCdJobRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
//        devopsCdJobRecordDTO.setLog(log.toString());
//        devopsCdJobRecordDTO.setCommandId(devopsHostCommandDTO.getId());
//        devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//    }
//
//    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void pipelineDeployDockerCompose(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId, StringBuilder log) {
//        LOGGER.info("start image deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
//
//        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(cdJobRecordId);
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
//        CiCdPipelineVO ciCdPipelineVO = devopsCiPipelineService.queryById(devopsCdPipelineRecordDTO.getPipelineId());
//        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsCdPipelineRecordDTO.getProjectId());
//        Long projectId = projectDTO.getId();
//        Long appServiceId = ciCdPipelineVO.getAppServiceId();
//
//        DevopsCdHostDeployInfoDTO devopsCdHostDeployInfoDTO = devopsCdHostDeployInfoService.queryById(devopsCdJobRecordDTO.getDeployInfoId());
//
//
//        if (ObjectUtils.isEmpty(devopsCdPipelineRecordDTO.getGitlabPipelineId())) {
//            throw new CommonException("devops.no.gitlab.pipeline.id");
//        }
//        Long appId = devopsCdHostDeployInfoDTO.getAppId();
//        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(appId);
//        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceId);
//
//        // 1. 查询关联构建任务生成的镜像
//        log.append("[info] Query deploy image info").append(System.lineSeparator());
//        CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageService.queryByGitlabPipelineId(appServiceId,
//                devopsCdPipelineRecordDTO.getGitlabPipelineId(),
//                devopsCdHostDeployInfoDTO.getImageJobName());
//        if (ciPipelineImageDTO == null) {
//            throw new CommonException("devops.deploy.images.not.exist");
//        }
//        log.append("[info] Deploy image is ").append(ciPipelineImageDTO.getImageTag()).append(System.lineSeparator());
//
//        // 2. 通过应用服务编码匹配service，替换镜像
//        log.append("[info] Start replace docker-compose.yaml").append(System.lineSeparator());
//        String value = replaceValue(appServiceDTO.getCode(),
//                ciPipelineImageDTO.getImageTag(),
//                dockerComposeValueService
//                        .baseQuery(devopsHostAppDTO.getEffectValueId())
//                        .getValue());
//
//        log.append("[info] Replace docker-compose.yaml result is : ").append(System.lineSeparator());
//        log.append(value).append(System.lineSeparator());
//
//        DockerComposeValueDTO dockerComposeValueDTO = new DockerComposeValueDTO();
//        dockerComposeValueDTO.setValue(value);
//
//        DockerComposeDeployVO dockerComposeDeployVO = new DockerComposeDeployVO();
//        dockerComposeDeployVO.setRunCommand((devopsCdHostDeployInfoDTO.getRunCommand()));
//        dockerComposeDeployVO.setDockerComposeValueDTO(dockerComposeValueDTO);
//        dockerComposeDeployVO.setAppName(devopsHostAppDTO.getName());
//
//        // 3. 更新docker-compose应用
//        log.append("[info] Start update app").append(System.lineSeparator());
//        DevopsHostCommandDTO devopsHostCommandDTO = dockerComposeService.updateDockerComposeApp(projectId, appId, cdJobRecordId, null, dockerComposeDeployVO, true);
//        log.append("[info] Update app success").append(System.lineSeparator());
//        devopsCdJobRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
//        devopsCdJobRecordDTO.setCommandId(devopsHostCommandDTO.getId());
//        devopsCdJobRecordDTO.setLog(log.toString());
//        devopsCdJobRecordService.update(devopsCdJobRecordDTO);
//    }
//
//    private String replaceValue(String code, String imageTag, String value) {
//        DumperOptions options = new DumperOptions();
//        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//        options.setAllowReadOnlyProperties(true);
//        options.setPrettyFlow(true);
//        Yaml yaml = new Yaml(options);
//        Object data = yaml.load(value);
//        JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
//        try {
//            Map<String, Object> services = (Map<String, Object>) jsonObject.get("services");
//
//            Map<String, Object> service = (Map<String, Object>) services.get(code);
//            if (!CollectionUtils.isEmpty(service)) {
//                service.replace("image", imageTag);
//            }
//            return yaml.dump(jsonObject);
//        } catch (Exception e) {
//            throw new CommonException(DEVOPS_YAML_FORMAT_INVALID, e);
//        }
//    }
//
//    protected DevopsHostAppDTO getDevopsHostAppDTO(Long projectId, Long hostId, String deployType, String appName, String appCode) {
//        if (org.apache.commons.lang3.StringUtils.equals(CREATE, deployType)) {
//            DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO();
//            devopsHostAppDTO.setRdupmType(RdupmTypeEnum.DOCKER.value());
//            devopsHostAppDTO.setProjectId(projectId);
//            devopsHostAppDTO.setHostId(hostId);
//            devopsHostAppDTO.setName(appName);
//            devopsHostAppDTO.setCode(appCode);
//            devopsHostAppDTO.setOperationType(OperationTypeEnum.CREATE_APP.value());
//            devopsHostAppMapper.insertSelective(devopsHostAppDTO);
//
//            return devopsHostAppMapper.selectByPrimaryKey(devopsHostAppDTO.getId());
//        } else {
//            //查询主机应用实例
//            DevopsHostAppDTO record = new DevopsHostAppDTO();
//            record.setRdupmType(RdupmTypeEnum.DOCKER.value());
//            record.setProjectId(projectId);
//            record.setHostId(hostId);
//            record.setName(appName);
//            record.setCode(appCode);
//            return devopsHostAppMapper.selectOne(record);
//        }
//    }
//
//    protected void updateStatusToSkip(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO, DevopsCdJobRecordDTO devopsCdJobRecordDTO) {
//        Long cdJobRecordId = devopsCdJobRecordDTO.getId();
//        Long projectId = devopsCdPipelineRecordDTO.getProjectId();
//        Long pipelineRecordId = devopsCdPipelineRecordDTO.getId();
//        Long stageRecordId = devopsCdJobRecordDTO.getStageRecordId();
//        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SKIPPED.toValue());
//        workFlowServiceOperator.approveUserTask(projectId, devopsCdPipelineRecordDTO.getBusinessKey(), MiscConstants.WORKFLOW_ADMIN_NAME, MiscConstants.WORKFLOW_ADMIN_ID, MiscConstants.WORKFLOW_ADMIN_ORG_ID);
//        devopsCdPipelineService.setAppDeployStatus(pipelineRecordId, stageRecordId, cdJobRecordId, true);
//    }
//
//    @Override
//    @Transactional
//    public void deleteByPipelineId(Long pipelineId) {
//        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
//        devopsCdPipelineRecordDTO.setPipelineId(pipelineId);
//        List<DevopsCdPipelineRecordDTO> devopsCdPipelineRecordDTOS = devopsCdPipelineRecordMapper.select(devopsCdPipelineRecordDTO);
//        if (!CollectionUtils.isEmpty(devopsCdPipelineRecordDTOS)) {
//            devopsCdPipelineRecordDTOS.forEach(cdPipelineRecordDTO -> devopsCdStageRecordService.deleteByPipelineRecordId(cdPipelineRecordDTO.getId()));
//        }
//        //删除cd 流水线记录
//        devopsCdPipelineRecordMapper.delete(devopsCdPipelineRecordDTO);
//    }
//
//    @Override
//    public DevopsCdPipelineRecordDTO queryById(Long id) {
//        Assert.notNull(id, PipelineCheckConstant.DEVOPS_PIPELINE_RECORD_ID_IS_NULL);
//        return devopsCdPipelineRecordMapper.selectByPrimaryKey(id);
//    }
//
//    @Override
//    @Transactional
//    public void updatePipelineStatusFailed(Long pipelineRecordId) {
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = queryById(pipelineRecordId);
//        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.FAILED.toValue());
//        update(devopsCdPipelineRecordDTO);
//    }
//
//    @Override
//    @Transactional
//    public void update(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO) {
//        devopsCdPipelineRecordDTO.setObjectVersionNumber(devopsCdPipelineRecordMapper.selectByPrimaryKey(devopsCdPipelineRecordDTO.getId()).getObjectVersionNumber());
//        if (devopsCdPipelineRecordMapper.updateByPrimaryKeySelective(devopsCdPipelineRecordDTO) != 1) {
//            throw new CommonException(ERROR_UPDATE_PIPELINE_RECORD_FAILED);
//        }
//    }
//
//    private void addAuditStateInfo(DevopsCdPipelineRecordVO devopsCdPipelineRecordVO) {
//        DevopsCdPipelineDeatilVO devopsCdPipelineDeatilVO = new DevopsCdPipelineDeatilVO();
//        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryStageWithPipelineRecordIdAndStatus(devopsCdPipelineRecordVO.getId(), PipelineStatus.NOT_AUDIT.toValue());
//        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
//            DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordDTOS.get(0);
//            // 继续判断阶段中是否还有待审核的任务
//            List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryJobWithStageRecordIdAndStatus(devopsCdStageRecordDTO.getId(), PipelineStatus.NOT_AUDIT.toValue());
//            if (!CollectionUtils.isEmpty(devopsCdJobRecordDTOS)) {
//                DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordDTOS.get(0);
//                DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = devopsCdAuditRecordService.queryByJobRecordIdAndUserId(devopsCdJobRecordDTO.getId(), DetailsHelper.getUserDetails().getUserId());
//                devopsCdPipelineDeatilVO.setType("task");
//                devopsCdPipelineDeatilVO.setStageName(devopsCdStageRecordDTO.getStageName());
//                devopsCdPipelineDeatilVO.setExecute(devopsCdAuditRecordDTO != null && AuditStatusEnum.NOT_AUDIT.value().equals(devopsCdAuditRecordDTO.getStatus()));
//                devopsCdPipelineDeatilVO.setStageRecordId(devopsCdStageRecordDTO.getId());
//                devopsCdPipelineDeatilVO.setTaskRecordId(devopsCdJobRecordDTO.getId());
//            }
//        }
//
//        devopsCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineDeatilVO);
//
//    }
//
//    @Override
//    public DevopsCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long cdPipelineId) {
//        if (Objects.isNull(cdPipelineId)) {
//            return null;
//        }
//        DevopsCdPipelineRecordDTO cdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(cdPipelineId);
//        if (Objects.isNull(cdPipelineRecordDTO)) {
//            return null;
//        }
//        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(cdPipelineRecordDTO.getCreatedBy());
//        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = ConvertUtils.convertObject(cdPipelineRecordDTO, DevopsCdPipelineRecordVO.class);
//        devopsCdPipelineRecordVO.setUsername(iamUserDTO.getRealName());
//        devopsCdPipelineRecordVO.setCreatedDate(cdPipelineRecordDTO.getCreationDate());
//        CiCdPipelineDTO ciCdPipelineDTO = devopsCiCdPipelineMapper.selectByPrimaryKey(cdPipelineRecordDTO.getPipelineId());
//        if (Objects.isNull(ciCdPipelineDTO)) {
//            return null;
//        }
//        AppServiceDTO serviceDTO = appServiceMapper.selectByPrimaryKey(ciCdPipelineDTO.getAppServiceId());
//        devopsCdPipelineRecordVO.setGitlabProjectId(serviceDTO.getGitlabProjectId());
//        //查询流水线信息
//        CiCdPipelineVO ciCdPipelineVO = devopsCiCdPipelineMapper.queryById(cdPipelineRecordDTO.getPipelineId());
//        //添加提交信息
//        devopsCdPipelineRecordVO.setCiCdPipelineVO(ciCdPipelineVO);
//        addCommitInfo(ciCdPipelineVO.getAppServiceId(), devopsCdPipelineRecordVO, cdPipelineRecordDTO);
//
//        devopsCdPipelineRecordVO.setCiCdPipelineVO(ciCdPipelineVO);
//        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(devopsCdPipelineRecordVO.getId());
//        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
//            List<DevopsCdStageRecordVO> devopsCdStageRecordVOS = ConvertUtils.convertList(devopsCdStageRecordDTOS, this::dtoToVo);
//            devopsCdStageRecordVOS.sort(Comparator.comparing(StageRecordVO::getSequence));
//            devopsCdStageRecordVOS.forEach(devopsCdStageRecordVO -> {
//                devopsCdStageRecordVO.setType(StageType.CD.getType());
//                //查询Cd job
//                List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(devopsCdStageRecordVO.getId());
//                List<DevopsCdJobRecordVO> devopsCdJobRecordVOS = ConvertUtils.convertList(devopsCdJobRecordDTOS, DevopsCdJobRecordVO.class);
//
//                // 根据job类型，添加相关信息
//                calculateJob(devopsCdJobRecordVOS);
//
//                devopsCdJobRecordVOS.sort(Comparator.comparing(DevopsCdJobRecordVO::getSequence));
//                devopsCdStageRecordVO.setJobRecordVOList(devopsCdJobRecordVOS);
//
//                //计算stage耗时
//                List<Long> collect = devopsCdJobRecordVOS.stream().filter(devopsCdJobRecordVO -> !Objects.isNull(devopsCdJobRecordVO.getDurationSeconds())).map(DevopsCdJobRecordVO::getDurationSeconds).collect(Collectors.toList());
//                if (!CollectionUtils.isEmpty(collect))
//                    devopsCdStageRecordVO.setDurationSeconds(collect.stream().reduce((aLong, aLong2) -> aLong + aLong2).get());
//            });
//            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(devopsCdStageRecordVOS);
//        } else {
//            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(Collections.emptyList());
//        }
//        // 计算流水线当前停留的审核节点
//        if (PipelineStatus.NOT_AUDIT.toValue().equals(devopsCdPipelineRecordVO.getStatus())) {
//            addAuditStateInfo(devopsCdPipelineRecordVO);
//        }
//
//        return devopsCdPipelineRecordVO;
//    }
//
//
//    protected void addCommitInfo(Long appServiceId, DevopsCdPipelineRecordVO devopsCPipelineRecordVO, DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO) {
//        DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(devopsCdPipelineRecordDTO.getCommitSha(), devopsCdPipelineRecordDTO.getRef());
//
//        CustomCommitVO customCommitVO = new CustomCommitVO();
//        devopsCPipelineRecordVO.setCommit(customCommitVO);
//
//        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
//        String gitlabProjectUrl;
//        if (appServiceDTO.getExternalConfigId() != null) {
//            AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
//            gitlabProjectUrl = appExternalConfigDTO.getRepositoryUrl();
//        } else {
//            GitlabProjectDTO gitlabProjectDTO = gitlabServiceClientOperator.queryProjectById(appServiceDTO.getGitlabProjectId());
//            gitlabProjectUrl = gitlabProjectDTO.getWebUrl();
//        }
//
//        customCommitVO.setGitlabProjectUrl(gitlabProjectUrl);
//
//        // 可能因为GitLab webhook 失败, commit信息查不出
//        if (devopsGitlabCommitDTO == null) {
//            return;
//        }
//        IamUserDTO commitUser = null;
//        if (devopsGitlabCommitDTO.getUserId() != null) {
//            commitUser = baseServiceClientOperator.queryUserByUserId(devopsGitlabCommitDTO.getUserId());
//        }
//
//        customCommitVO.setRef(devopsCdPipelineRecordDTO.getRef());
//        customCommitVO.setCommitSha(devopsCdPipelineRecordDTO.getCommitSha());
//        customCommitVO.setCommitContent(devopsGitlabCommitDTO.getCommitContent());
//        customCommitVO.setCommitUrl(devopsGitlabCommitDTO.getUrl());
//
//        if (commitUser != null) {
//            customCommitVO.setUserHeadUrl(commitUser.getImageUrl());
//            customCommitVO.setUserName(commitUser.getLdap() ? commitUser.getLoginName() : commitUser.getEmail());
//        }
//    }
//
//    protected void calculateJob(List<DevopsCdJobRecordVO> devopsCdJobRecordVOS) {
//        devopsCdJobRecordVOS.forEach(devopsCdJobRecordVO -> {
//            //如果是自动部署返回 能点击查看生成实例的相关信息
//            if (JobTypeEnum.CD_DEPLOY.value().equals(devopsCdJobRecordVO.getType())) {
//                //部署环境 应用服务 生成版本 实例名称
//                Long commandId = devopsCdJobRecordVO.getCommandId();
//                if (commandId != null) {
//                    DeployRecordVO deployRecordVO = devopsDeployRecordService.queryEnvDeployRecordByCommandId(commandId);
//                    if (deployRecordVO != null) {
//                        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(deployRecordVO.getAppId());
//                        DevopsCdJobRecordVO.CdAuto cdAuto = devopsCdJobRecordVO.new CdAuto();
//                        cdAuto.setAppServiceName(deployRecordVO.getDeployObjectName());
//                        cdAuto.setAppServiceVersion(deployRecordVO.getDeployObjectVersion());
//                        cdAuto.setEnvId(deployRecordVO.getEnvId());
//                        cdAuto.setEnvName(deployRecordVO.getDeployPayloadName());
//                        cdAuto.setAppId(deployRecordVO.getAppId());
//                        cdAuto.setAppName(deployRecordVO.getAppName());
//                        if (!ObjectUtils.isEmpty(devopsDeployAppCenterEnvDTO)) {
//                            cdAuto.setRdupmType(devopsDeployAppCenterEnvDTO.getRdupmType());
//                            cdAuto.setChartSource(devopsDeployAppCenterEnvDTO.getChartSource());
//                            cdAuto.setOperationType(devopsDeployAppCenterEnvDTO.getOperationType());
//                            cdAuto.setDeployType(ENV);
//                            cdAuto.setDeployTypeId(devopsDeployAppCenterEnvDTO.getEnvId());
//                            if (RdupmTypeEnum.CHART.value().equals(devopsDeployAppCenterEnvDTO.getRdupmType())) {
//                                AppServiceInstanceInfoVO appServiceInstanceInfoVO = appServiceInstanceService.queryInfoById(devopsDeployAppCenterEnvDTO.getProjectId(), devopsDeployAppCenterEnvDTO.getObjectId());
//                                if (!ObjectUtils.isEmpty(appServiceInstanceInfoVO)) {
//                                    cdAuto.setAppServiceId(appServiceInstanceInfoVO.getAppServiceId());
//                                    cdAuto.setStatus(appServiceInstanceInfoVO.getStatus());
//                                }
//                            }
//                        }
//                        devopsCdJobRecordVO.setCdAuto(cdAuto);
//                    }
//                }
//            }
//
//            //部署组部署 返回对应信息
//            if (JobTypeEnum.CD_DEPLOYMENT.value().equals(devopsCdJobRecordVO.getType())) {
//                //部署环境 应用服务 生成版本 实例名称
//                Long commandId = devopsCdJobRecordVO.getCommandId();
//                if (commandId != null) {
//                    DeployRecordVO deployRecordVO = devopsDeployRecordService.queryEnvDeployRecordByCommandId(commandId);
//                    if (deployRecordVO != null) {
//                        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(deployRecordVO.getAppId());
//                        DevopsCdJobRecordVO.CdAuto cdAuto = devopsCdJobRecordVO.new CdAuto();
//                        cdAuto.setEnvId(deployRecordVO.getEnvId());
//                        cdAuto.setEnvName(deployRecordVO.getDeployPayloadName());
//                        cdAuto.setAppId(deployRecordVO.getAppId());
//                        cdAuto.setAppName(deployRecordVO.getAppName());
//                        if (!ObjectUtils.isEmpty(devopsDeployAppCenterEnvDTO)) {
//                            cdAuto.setOperationType(devopsDeployAppCenterEnvDTO.getOperationType());
//                            cdAuto.setRdupmType(devopsDeployAppCenterEnvDTO.getRdupmType());
//                            cdAuto.setDeployType(ENV);
//                            cdAuto.setDeployTypeId(devopsDeployAppCenterEnvDTO.getEnvId());
//                            if (RdupmTypeEnum.DEPLOYMENT.value().equals(devopsDeployAppCenterEnvDTO.getRdupmType())) {
//                                DevopsDeploymentDTO deploymentDTO = devopsDeploymentService.selectByPrimaryKey(devopsDeployAppCenterEnvDTO.getObjectId());
//                                if (!ObjectUtils.isEmpty(deploymentDTO)) {
//                                    cdAuto.setStatus(deploymentDTO.getStatus());
//                                }
//                            }
//                        }
//                        devopsCdJobRecordVO.setCdAuto(cdAuto);
//                    }
//                }
//            }
//
//            //如果是人工审核返回审核信息
//            if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobRecordVO.getType())) {
//                // 指定审核人员 已审核人员 审核状态
//                DevopsCdJobRecordVO.Audit audit = devopsCdJobRecordVO.new Audit();
//                List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(devopsCdJobRecordVO.getId());
//                if (!CollectionUtils.isEmpty(devopsCdAuditRecordDTOS)) {
//                    List<Long> uids = devopsCdAuditRecordDTOS.stream().map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList());
//                    List<IamUserDTO> allIamUserDTOS = baseServiceClientOperator.listUsersByIds(uids);
//
//                    List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(devopsCdAuditRecordDTOS.stream()
//                            .filter(devopsCdAuditRecordDTO -> AuditStatusEnum.PASSED.value().equals(devopsCdAuditRecordDTO.getStatus()) || AuditStatusEnum.REFUSED.value().equals(devopsCdAuditRecordDTO.getStatus()))
//                            .map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList()));
//                    audit.setReviewedUsers(iamUserDTOS);
//                    audit.setAppointUsers(allIamUserDTOS);
//                    audit.setStatus(devopsCdJobRecordVO.getStatus());
//                }
//                devopsCdJobRecordVO.setAudit(audit);
//            }
//            //如果是主机部署 显示主机部署模式(镜像，jar，自定义)，来源，关联构建任务
//            if (JobTypeEnum.CD_HOST.value().equals(devopsCdJobRecordVO.getType())) {
//                Long commandId = devopsCdJobRecordVO.getCommandId();
//                if (commandId != null) {
//                    DeployRecordVO deployRecordVO = devopsDeployRecordService.queryHostDeployRecordByCommandId(commandId);
//                    DevopsCdJobRecordVO.CdAuto cdAuto = devopsCdJobRecordVO.new CdAuto();
//                    cdAuto.setAppId(deployRecordVO.getAppId());
//                    cdAuto.setAppName(deployRecordVO.getAppName());
//                    cdAuto.setHostName(deployRecordVO.getDeployPayloadName());
//                    cdAuto.setDeployType(HOST);
//                    cdAuto.setDeployTypeId(deployRecordVO.getDeployPayloadId());
//                    cdAuto.setOperationType(deployRecordVO.getDeployObjectType());
//                    cdAuto.setRdupmType(deployRecordVO.getDeployObjectType());
//                    DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(deployRecordVO.getAppId());
//                    if (!ObjectUtils.isEmpty(devopsHostAppDTO)) {
//                        cdAuto.setOperationType(devopsHostAppDTO.getOperationType());
//                    }
//                    devopsCdJobRecordVO.setCdAuto(cdAuto);
//                }
//            }
//
//            if (JobTypeEnum.CD_API_TEST.value().equals(devopsCdJobRecordVO.getType())
//                    && devopsCdJobRecordVO.getApiTestTaskRecordId() != null) {
//                try {
//                    ApiTestTaskRecordVO apiTestTaskRecordVO = testServiceClientoperator.queryById(devopsCdJobRecordVO.getProjectId(), devopsCdJobRecordVO.getApiTestTaskRecordId());
//                    CdApiTestConfigVO cdApiTestConfigVO = gson.fromJson(devopsCdJobRecordVO.getMetadata(), CdApiTestConfigVO.class);
//                    apiTestTaskRecordVO.setDeployJobName(cdApiTestConfigVO.getDeployJobName());
//                    apiTestTaskRecordVO.setPerformThreshold(cdApiTestConfigVO.getWarningSettingVO().getPerformThreshold());
//                    devopsCdJobRecordVO.setApiTestTaskRecordVO(apiTestTaskRecordVO);
//                } catch (Exception ex) {
//                    LOGGER.warn("Failed to query api test task record..., the ex code is {}", ex.getMessage());
//                }
//            }
//
//            if (JobTypeEnum.CD_EXTERNAL_APPROVAL.value().equals(devopsCdJobRecordVO.getType())) {
//                ExternalApprovalJobVO externalApprovalJobVO = gson.fromJson(devopsCdJobRecordVO.getMetadata(), ExternalApprovalJobVO.class);
//                devopsCdJobRecordVO.setExternalApprovalJobVO(externalApprovalJobVO);
//            }
//            if (devopsCdJobRecordVO.getStartedDate() != null && devopsCdJobRecordVO.getFinishedDate() != null) {
//                long durationSeconds = (devopsCdJobRecordVO.getFinishedDate().getTime() - devopsCdJobRecordVO.getStartedDate().getTime()) / 1000;
//                devopsCdJobRecordVO.setDurationSeconds(durationSeconds == 0 ? 1 : durationSeconds);
//            }
//        });
//
//    }
//
////    @Override
////    public Boolean testConnection(HostConnectionVO hostConnectionVO) {
////        SSHClient ssh = new SSHClient();
////        Session session = null;
////        Boolean index = true;
////        try {
////            sshUtil.sshConnect(hostConnectionVO, ssh);
////            session = ssh.startSession();
////            Session.Command cmd = session.exec("echo Hello World!!!");
////            if (LOGGER.isInfoEnabled()) {
////                LOGGER.info(IOUtils.readFully(cmd.getInputStream()).toString());
////            }
////            cmd.join(5, TimeUnit.SECONDS);
////            if (LOGGER.isInfoEnabled()) {
////                LOGGER.info("** exit status: {}", cmd.getExitStatus());
////            }
////            if (cmd.getExitStatus() != 0) {
////                throw new CommonException("devops.test.connection");
////            }
////        } catch (IOException e) {
////            index = false;
////            LOGGER.error("error ssh connect", e);
////        } finally {
////            closeSsh(ssh, session);
////        }
////        return index;
////    }
//
//    private DevopsCdStageRecordVO dtoToVo(DevopsCdStageRecordDTO devopsCdStageRecordDTO) {
//        DevopsCdStageRecordVO devopsCdStageRecordVO = new DevopsCdStageRecordVO();
//        BeanUtils.copyProperties(devopsCdStageRecordDTO, devopsCdStageRecordVO);
//        devopsCdStageRecordVO.setName(devopsCdStageRecordDTO.getStageName());
//        return devopsCdStageRecordVO;
//    }
//
//    @Override
//    public DevopsCdPipelineRecordVO queryByCdPipelineRecordId(Long cdPipelineRecordId) {
//        if (cdPipelineRecordId == null || cdPipelineRecordId == 0L) {
//            return null;
//        }
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(cdPipelineRecordId);
//        if (Objects.isNull(devopsCdPipelineRecordDTO)) {
//            return null;
//        }
//        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = new DevopsCdPipelineRecordVO();
//        BeanUtils.copyProperties(devopsCdPipelineRecordDTO, devopsCdPipelineRecordVO);
//        devopsCdPipelineRecordVO.setCreatedDate(devopsCdPipelineRecordDTO.getCreationDate());
//        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(devopsCdPipelineRecordVO.getId());
//        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
//            //封装审核数据
//            List<DevopsCdStageRecordVO> devopsCdStageRecordVOS = ConvertUtils.convertList(devopsCdStageRecordDTOS, DevopsCdStageRecordVO.class);
//            devopsCdStageRecordVOS.sort(Comparator.comparing(StageRecordVO::getSequence));
//            // 计算流水线当前停留的审核节点
//            if (PipelineStatus.NOT_AUDIT.toValue().equals(devopsCdPipelineRecordDTO.getStatus())) {
//                addAuditStateInfo(devopsCdPipelineRecordVO);
//            }
//            devopsCdStageRecordVOS.forEach(devopsCdStageRecordVO -> {
//                List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(devopsCdStageRecordVO.getId());
//                List<DevopsCdJobRecordVO> devopsCdJobRecordVOS = ConvertUtils.convertList(devopsCdJobRecordDTOS, DevopsCdJobRecordVO.class);
//
//                //计算stage耗时
//                List<Long> collect = devopsCdJobRecordVOS.stream().filter(devopsCdJobRecordVO -> !Objects.isNull(devopsCdJobRecordVO.getDurationSeconds())).map(DevopsCdJobRecordVO::getDurationSeconds).collect(Collectors.toList());
//                if (!CollectionUtils.isEmpty(collect))
//                    devopsCdStageRecordVO.setDurationSeconds(collect.stream().reduce((aLong, aLong2) -> aLong + aLong2).get());
//            });
//
//            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(devopsCdStageRecordVOS);
//        } else {
//            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(Collections.emptyList());
//        }
//
//        return devopsCdPipelineRecordVO;
//    }
//
//}
