package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.CUSTOM_REPO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.commons.lang.BooleanUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoImageTagVo;
import io.choerodon.devops.api.vo.pipeline.ExternalApprovalJobVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.api.vo.test.ApiTestTaskRecordVO;
import io.choerodon.devops.app.eventhandler.payload.HostDeployPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.*;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineStageDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineTaskDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.deploy.DockerInstanceStatusEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostInstanceType;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.feign.operator.TestServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 10:41
 */
@Service
public class DevopsCdPipelineRecordServiceImpl implements DevopsCdPipelineRecordService {

    private static final String ERROR_SAVE_PIPELINE_RECORD_FAILED = "error.save.pipeline.record.failed";
    private static final String ERROR_UPDATE_PIPELINE_RECORD_FAILED = "error.update.pipeline.record.failed";

    public static final Logger LOGGER = LoggerFactory.getLogger(DevopsCdPipelineRecordServiceImpl.class);

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private static final BASE64Decoder decoder = new BASE64Decoder();
    @Autowired
    private DevopsCdAuditRecordService devopsCdAuditRecordService;

    @Autowired
    private DevopsCdJobRecordService devopsCdJobRecordService;

    @Autowired
    private DevopsCdPipelineRecordMapper devopsCdPipelineRecordMapper;

    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;

    @Autowired
    private DevopsCdStageRecordService devopsCdStageRecordService;

    @Autowired
    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;

    @Autowired
    private RdupmClientOperator rdupmClientOperator;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;

    @Autowired
    private AppServiceMapper appServiceMapper;

    @Autowired
    private DevopsCdAuditService devopsCdAuditService;

    @Autowired
    private TransactionalProducer producer;

    @Autowired
    private DevopsCdStageMapper devopsCdStageMapper;

    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;

    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;

    @Autowired
    private AppServiceService applicationService;

    @Autowired
    private CiPipelineImageService ciPipelineImageService;

    @Autowired
    private CiPipelineMavenService ciPipelineMavenService;

    @Autowired
    private TestServiceClientOperator testServiceClientoperator;

    @Autowired
    private DevopsHostMapper devopsHostMapper;

    @Autowired
    private SshUtil sshUtil;

    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;

    @Autowired
    private DevopsDockerInstanceMapper devopsDockerInstanceMapper;

    @Autowired
    private DevopsHostCommandService devopsHostCommandService;

    @Autowired
    private DevopsHostService devopsHostService;

    @Autowired
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    private WorkFlowServiceOperator workFlowServiceOperator;
    @Autowired
    private DevopsNormalInstanceMapper devopsNormalInstanceMapper;
    @Autowired
    private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    private DevopsHostAppInstanceRelService devopsHostAppInstanceRelService;
    @Autowired
    private DevopsNormalInstanceService devopsNormalInstanceService;
    @Autowired
    @Lazy
    private DevopsCdPipelineService devopsCdPipelineService;

    @Override
    public DevopsCdPipelineRecordDTO queryByGitlabPipelineId(Long gitlabPipelineId) {
        Assert.notNull(gitlabPipelineId, PipelineCheckConstant.ERROR_GITLAB_PIPELINE_ID_IS_NULL);
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
        devopsCdPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
        return devopsCdPipelineRecordMapper.selectOne(devopsCdPipelineRecordDTO);
    }

    @Override
    @Transactional
    public void save(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO) {
        if (devopsCdPipelineRecordMapper.insertSelective(devopsCdPipelineRecordDTO) != 1) {
            throw new CommonException(ERROR_SAVE_PIPELINE_RECORD_FAILED);
        }
    }

    @Override
    @Transactional
    public void updateStatusById(Long pipelineRecordId, String status) {
        DevopsCdPipelineRecordDTO pipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);

        // 已取消的流水线 不能更新为成功、失败状态
        if (pipelineRecordDTO.getStatus().equals(PipelineStatus.CANCELED.toValue())
                && (status.equals(PipelineStatus.FAILED.toValue())
                || status.equals(PipelineStatus.SUCCESS.toValue()))) {
            LOGGER.info("cancel pipeline can not update status!! pipeline record Id {}", pipelineRecordDTO.getId());
            return;
        }

        pipelineRecordDTO.setStatus(status);
        if (devopsCdPipelineRecordMapper.updateByPrimaryKey(pipelineRecordDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_PIPELINE_RECORD_FAILED);
        }
    }

    /**
     * 准备workflow创建实例所需数据
     * 为此workflow下所有stage创建记录
     */
    @Override
    public DevopsPipelineDTO createCDWorkFlowDTO(Long pipelineRecordId, Boolean isRetry) {
        // 1.
        DevopsPipelineDTO devopsPipelineDTO = new DevopsPipelineDTO();
        devopsPipelineDTO.setPipelineRecordId(pipelineRecordId);
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
        devopsPipelineDTO.setPipelineName(devopsCdPipelineRecordDTO.getPipelineName());
        devopsPipelineDTO.setBusinessKey(devopsCdPipelineRecordDTO.getBusinessKey());

        // 2.
        List<DevopsPipelineStageDTO> devopsPipelineStageDTOS = new ArrayList<>();
        List<DevopsCdStageRecordDTO> stageRecordDTOList;
        if (BooleanUtils.isTrue(isRetry)) {
            stageRecordDTOList = devopsCdStageRecordMapper.queryRetryStage(pipelineRecordId);
        } else {
            stageRecordDTOList = devopsCdStageRecordService.queryByPipelineRecordId(pipelineRecordId);
        }

        if (CollectionUtils.isEmpty(stageRecordDTOList)) {
            return null;
        }
        stageRecordDTOList = stageRecordDTOList.stream().sorted(Comparator.comparing(DevopsCdStageRecordDTO::getSequence)).collect(Collectors.toList());

        for (int i = 0; i < stageRecordDTOList.size(); i++) {
            // 3.
            DevopsPipelineStageDTO stageDTO = new DevopsPipelineStageDTO();
            DevopsCdStageRecordDTO stageRecordDTO = stageRecordDTOList.get(i);
            stageDTO.setStageRecordId(stageRecordDTO.getId());
            // 4.
            List<DevopsCdJobRecordDTO> jobRecordDTOList;
            if (Boolean.FALSE.equals(isRetry) || i > 0) {
                jobRecordDTOList = devopsCdJobRecordService.queryByStageRecordId(stageRecordDTO.getId());
            } else {
                jobRecordDTOList = devopsCdJobRecordMapper.queryRetryJob(stageRecordDTO.getId());
            }
            jobRecordDTOList = jobRecordDTOList.stream().sorted(Comparator.comparing(DevopsCdJobRecordDTO::getSequence)).collect(Collectors.toList());
            List<DevopsPipelineTaskDTO> taskDTOList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(jobRecordDTOList)) {
                jobRecordDTOList.forEach(jobRecordDTO -> {
                    DevopsPipelineTaskDTO taskDTO = new DevopsPipelineTaskDTO();
                    taskDTO.setTaskRecordId(jobRecordDTO.getId());
                    taskDTO.setTaskName(jobRecordDTO.getName());
                    if (jobRecordDTO.getType().equals(JobTypeEnum.CD_AUDIT.value())) {
                        List<DevopsCdAuditRecordDTO> jobAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(jobRecordDTO.getId());
                        if (CollectionUtils.isEmpty(jobAuditRecordDTOS)) {
                            throw new CommonException("error.audit.job.noUser");
                        }
                        List<String> taskUsers = jobAuditRecordDTOS.stream().map(t -> TypeUtil.objToString(t.getUserId())).collect(Collectors.toList());
                        taskDTO.setUsernames(taskUsers);
                        taskDTO.setMultiAssign(taskUsers.size() > 1);
                    } else if (jobRecordDTO.getType().equals(JobTypeEnum.CD_API_TEST.value())) {
                        CdApiTestConfigVO cdApiTestConfigVO = JsonHelper.unmarshalByJackson(jobRecordDTO.getMetadata(), CdApiTestConfigVO.class);
                        taskDTO.setDeployJobName(cdApiTestConfigVO.getDeployJobName());
                    }
                    taskDTO.setTaskType(jobRecordDTO.getType());
                    if (jobRecordDTO.getCountersigned() != null) {
                        taskDTO.setSign(jobRecordDTO.getCountersigned().longValue());
                    }
                    taskDTOList.add(taskDTO);
                });
            }
            stageDTO.setTasks(taskDTOList);
            // 5. 审核任务处理
            // 在workflow 是先渲染阶段 在渲染阶段任务
            if (i != stageRecordDTOList.size() - 1) {
                stageDTO.setNextStageTriggerType(TriggerTypeEnum.AUTO.value());
            }
            devopsPipelineStageDTOS.add(stageDTO);
        }
        devopsPipelineDTO.setStages(devopsPipelineStageDTOS);
        return devopsPipelineDTO;
    }

    @Override
    public DevopsPipelineDTO createCDWorkFlowDTO(Long pipelineRecordId) {
        return createCDWorkFlowDTO(pipelineRecordId, false);
    }

    private String getRegexStr(CdHostDeployConfigVO.ImageDeploy imageDeploy) {
        String regexStr = null;
        if (!StringUtils.isEmpty(imageDeploy.getMatchType())
                && !StringUtils.isEmpty(imageDeploy.getMatchContent())) {
            CiTriggerType ciTriggerType = CiTriggerType.forValue(imageDeploy.getMatchType());
            if (ciTriggerType != null) {
                String triggerValue = imageDeploy.getMatchContent();
                switch (ciTriggerType) {
                    case REFS:
                        regexStr = "^.*" + triggerValue + ".*$";
                        break;
                    case EXACT_MATCH:
                        regexStr = "^" + triggerValue + "$";
                        break;
                    case REGEX_MATCH:
                        regexStr = triggerValue;
                        break;
                    case EXACT_EXCLUDE:
                        regexStr = "^(?!.*" + triggerValue + ").*$";
                        break;
                }
            }
        }
        return regexStr;
    }

    private String getMavenVersion(String version) {
        if (version.contains(BaseConstants.Symbol.SLASH)) {
            return version.split(BaseConstants.Symbol.SLASH)[0];
        } else {
            return version;
        }
    }

    private void closeSsh(SSHClient ssh, Session session) {
        try {
            if (session != null) {
                session.close();
            }
            ssh.disconnect();
        } catch (IOException e) {
            LOGGER.error("error close ssh", e);
        }
    }

    private String getJarName(String url) {
        String[] arr = url.split("/");
        return arr[arr.length - 1].replace(".jar", "-") + GenerateUUID.generateRandomString() + ".jar";
    }

    @Override
    @Async
    @Transactional
    public void cdHostDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        HostDeployPayload hostDeployPayload = new HostDeployPayload(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        DevopsCdPipelineRecordDTO pipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
        CustomContextUtil.setUserContext(pipelineRecordDTO.getCreatedBy());

        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(hostDeployPayload.getJobRecordId());
        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        try {
            if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.IMAGED_DEPLOY.getValue())) {
                ApplicationContextHelper
                        .getSpringFactory()
                        .getBean(DevopsCdPipelineRecordService.class)
                        .pipelineDeployImage(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId());
            } else if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.JAR_DEPLOY.getValue())) {
                ApplicationContextHelper
                        .getSpringFactory()
                        .getBean(DevopsCdPipelineRecordService.class)
                        .pipelineDeployJar(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId());
            } else {
                ApplicationContextHelper
                        .getSpringFactory()
                        .getBean(DevopsCdPipelineRecordService.class)
                        .pipelineCustomDeploy(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId());
            }
        } catch (Exception e) {
            LOGGER.error(" deploy failed!, error msg is", e);
            devopsCdJobRecordService.updateJobStatusFailed(cdJobRecordId, LogUtil.cutOutString(LogUtil.readContentOfThrowable(e), 2500));
            devopsCdStageRecordService.updateStageStatusFailed(cdStageRecordId);
            updatePipelineStatusFailed(pipelineRecordId);
            workFlowServiceOperator.stopInstance(pipelineRecordDTO.getProjectId(), pipelineRecordDTO.getBusinessKey());
        }


    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void pipelineCustomDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        LOGGER.info("start custom deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
        // 0.1 查询部署信息
        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        String value = null;
        try {
            value = new String(decoder.decodeBuffer(cdHostDeployConfigVO.getCustomize().getValues()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException(e);
        }

        Long hostId = cdHostDeployConfigVO.getHostConnectionVO().getHostId();


        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.CUSTOM_DEPLOY.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setCdJobRecordId(cdJobRecordId);
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandDTO.setInstanceType(HostInstanceType.CUSTOM.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        String commands = genCustomCommands(value);

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.CUSTOM_DEPLOY.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(commands);

        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.PIPELINE_CUSTOM_DEPLOY, hostId, cdJobRecordId),
                JsonHelper.marshalByJackson(hostAgentMsgVO));


    }

    private String genCustomCommands(String value) {
        String[] strings = value.split("\n");
        String commands = "";
        for (String s : strings) {
            if (s.length() > 0 && !s.contains("#")) {
                commands = commands + s;
            }
        }
        return "#!/bin/bash\n" + commands;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void pipelineDeployJar(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        LOGGER.info("start jar deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);

        DevopsCdPipelineRecordDTO cdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordService.queryById(cdJobRecordId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(cdPipelineRecordDTO.getProjectId());
        Long projectId = projectDTO.getId();

        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);

        Long hostId = cdHostDeployConfigVO.getHostConnectionVO().getHostId();
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);

        CdHostDeployConfigVO.JarDeploy jarDeploy = cdHostDeployConfigVO.getJarDeploy();


        // 0.1 从制品库获取仓库信息
        Long nexusRepoId;
        String groupId;
        String artifactId;
        String versionRegular;

        if (jarDeploy.getDeploySource().equals(HostDeploySource.MATCH_DEPLOY.getValue())) {
            nexusRepoId = jarDeploy.getRepositoryId();
            groupId = jarDeploy.getGroupId();
            artifactId = jarDeploy.getArtifactId();
            versionRegular = jarDeploy.getVersionRegular();
        } else {
            if (ObjectUtils.isEmpty(cdPipelineRecordDTO.getGitlabPipelineId())) {
                throw new CommonException("error.no.gitlab.pipeline.id");
            }
            CiPipelineMavenDTO ciPipelineMavenDTO = ciPipelineMavenService.queryByGitlabPipelineId(cdPipelineRecordDTO.getGitlabPipelineId(), jarDeploy.getPipelineTask());
            nexusRepoId = ciPipelineMavenDTO.getNexusRepoId();
            groupId = ciPipelineMavenDTO.getGroupId();
            artifactId = ciPipelineMavenDTO.getArtifactId();
            //0.0.1-SNAPSHOT/springbbot-0.0.1-20210506.081037-4
            versionRegular = "^" + getMavenVersion(ciPipelineMavenDTO.getVersion()) + "$";
        }

        // 0.3 获取并记录信息
        List<C7nNexusComponentDTO> nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), cdPipelineRecordDTO.getProjectId(), nexusRepoId, groupId, artifactId, versionRegular);
        if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
            LOGGER.info("no jar to deploy,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
            updateStatusToSkip(cdPipelineRecordDTO, jobRecordDTO);
            return;
        }
        List<NexusMavenRepoDTO> mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), cdPipelineRecordDTO.getProjectId(), Collections.singleton(nexusRepoId));
        if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
            throw new CommonException("error.get.maven.config");
        }

        C7nNexusComponentDTO c7nNexusComponentDTO = nexusComponentDTOList.get(0);

        JarDeployVO jarDeployVO = null;
        try {
            jarDeployVO = new JarDeployVO(AppSourceType.CURRENT_PROJECT.getValue(),
                    new String(decoder.decodeBuffer(jarDeploy.getValue()), StandardCharsets.UTF_8),
                    new ProdJarInfoVO(nexusRepoId,
                            groupId,
                            artifactId,
                            c7nNexusComponentDTO.getVersion()));
        } catch (IOException e) {
            throw new CommonException(e);
        }


        JarPullInfoDTO jarPullInfoDTO = new JarPullInfoDTO();
        jarPullInfoDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
        jarPullInfoDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
        jarPullInfoDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());


        // 1.更新流水线状态 记录信息
        jobRecordDTO.setDeployMetadata(gson.toJson(jarPullInfoDTO));
        jobRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
        jobRecordDTO.setStartedDate(new Date());
        devopsCdJobRecordService.update(jobRecordDTO);

        // 2. 执行jar部署
        devopsCdEnvDeployInfoService.updateOrUpdateByCdJob(jobRecordDTO.getJobId(), getJarName(jarPullInfoDTO.getDownloadUrl()));

        // 2.保存记录
        String instanceName = jarDeploy.getName() != null ? jarDeploy.getName() : c7nNexusComponentDTO.getName();

        DevopsNormalInstanceDTO devopsNormalInstanceDTO = devopsNormalInstanceService.queryByHostIdAndName(hostId, instanceName);
        if (devopsNormalInstanceDTO == null) {
            devopsNormalInstanceDTO = new DevopsNormalInstanceDTO(hostId,
                    instanceName,
                    AppSourceType.CURRENT_PROJECT.getValue(),
                    HostResourceType.JAVA_PROCESS.value());
            MapperUtil.resultJudgedInsertSelective(devopsNormalInstanceMapper, devopsNormalInstanceDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);

        } else {
            // 删除原有应用关联关系
            devopsHostAppInstanceRelService.deleteByHostIdAndInstanceInfo(hostId, devopsNormalInstanceDTO.getId(), HostInstanceType.NORMAL_PROCESS.value());
        }
        // 有关联的应用，则保存关联关系
        List<AppServiceDTO> appServiceDTOList = applicationService.listByProjectIdAndGAV(projectId, groupId, artifactId);
        if (!CollectionUtils.isEmpty(appServiceDTOList)) {
            Set<Long> appIds = appServiceDTOList.stream().map(AppServiceDTO::getId).collect(Collectors.toSet());
            Map<Long, AppServiceDTO> appServiceDTOMap = appServiceDTOList.stream().collect(Collectors.toMap(AppServiceDTO::getId, Function.identity()));
            Long instanceId = devopsNormalInstanceDTO.getId();
            appIds.forEach(appId -> devopsHostAppInstanceRelService.saveHostAppInstanceRel(projectId,
                    hostId,
                    appId,
                    AppSourceType.CURRENT_PROJECT.getValue(),
                    instanceId,
                    HostInstanceType.NORMAL_PROCESS.value(), appServiceDTOMap.get(appId) == null ? null : appServiceDTOMap.get(appId).getName()));
        }

        JavaDeployDTO javaDeployDTO = new JavaDeployDTO(jarPullInfoDTO,
                instanceName,
                c7nNexusComponentDTO.getName(),
                String.valueOf(devopsNormalInstanceDTO.getId()),
                HostDeployUtil.genJavaRunCmd(jarPullInfoDTO, jarDeployVO, devopsNormalInstanceDTO.getId()),
                devopsNormalInstanceDTO.getPid());


        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_JAR.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setCdJobRecordId(cdJobRecordId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.JAVA_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsNormalInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        // 保存执行记录
        devopsDeployRecordService.saveRecord(
                jobRecordDTO.getProjectId(),
                DeployType.AUTO,
                null,
                DeployModeEnum.HOST,
                hostId,
                devopsHostDTO != null ? devopsHostDTO.getName() : null,
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.JAR,
                c7nNexusComponentDTO.getName(),
                c7nNexusComponentDTO.getVersion(),
                null,
                new DeploySourceVO(AppSourceType.CURRENT_PROJECT, projectDTO.getName()));

        // 3. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_JAR.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaDeployDTO));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.JAVA_INSTANCE, hostId, devopsNormalInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void pipelineDeployImage(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        LOGGER.info("start image deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);

        String deployVersion = null;
        String deployObjectName = null;
        String image = null;
        Long appServiceId = null;
        String serviceName = null;

        DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryById(cdJobRecordId);

        DockerDeployDTO dockerDeployDTO = new DockerDeployDTO();
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsCdPipelineRecordDTO.getProjectId());
        Long projectId = projectDTO.getId();

        CdHostDeployConfigVO cdHostDeployConfigVO = JsonHelper.unmarshalByJackson(devopsCdJobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        CdHostDeployConfigVO.ImageDeploy imageDeploy = cdHostDeployConfigVO.getImageDeploy();

        Long hostId = cdHostDeployConfigVO.getHostConnectionVO().getHostId();
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);

        // 0.2
        if (imageDeploy.getDeploySource().equals(HostDeploySource.MATCH_DEPLOY.getValue())) {
            HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(imageDeploy.getRepoType(), TypeUtil.objToLong(imageDeploy.getRepoId()), imageDeploy.getImageName(), null);
            List<HarborC7nImageTagVo> filterImageTagVoList;
            if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
                LOGGER.info("no image to deploy,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
                updateStatusToSkip(devopsCdPipelineRecordDTO, devopsCdJobRecordDTO);
                return;
            } else {
                String pattern = getRegexStr(imageDeploy);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> cd deploy pattern is :{}, filterImageTagVoList is : {} <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", pattern, JsonHelper.marshalByJackson(imageTagVo.getImageTagList()));
                }
                filterImageTagVoList = imageTagVo.getImageTagList().stream().filter(t -> Pattern.matches(pattern, t.getTagName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(filterImageTagVoList)) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("no image to deploy,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
                    }
                    updateStatusToSkip(devopsCdPipelineRecordDTO, devopsCdJobRecordDTO);
                    return;
                }
            }
            image = filterImageTagVoList.get(0).getPullCmd().replace("docker pull", "");
            // 设置拉取账户
            dockerDeployDTO.setDockerPullAccountDTO(new DockerPullAccountDTO(
                    imageTagVo.getHarborUrl(),
                    imageTagVo.getPullAccount(),
                    imageTagVo.getPullPassword()));

            deployVersion = filterImageTagVoList.get(0).getTagName();
            deployObjectName = imageDeploy.getImageName();
            AppServiceRepVO appServiceRepVO = applicationService.queryByCode(projectId, imageDeploy.getImageName());
            appServiceId = appServiceRepVO == null ? null : appServiceRepVO.getId();
        } else {
            if (ObjectUtils.isEmpty(devopsCdPipelineRecordDTO.getGitlabPipelineId())) {
                throw new CommonException("error.no.gitlab.pipeline.id");
            }
            CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageService.queryByGitlabPipelineId(devopsCdPipelineRecordDTO.getGitlabPipelineId(), imageDeploy.getPipelineTask());
            HarborRepoDTO harborRepoDTO = rdupmClientOperator.queryHarborRepoConfigById(devopsCdPipelineRecordDTO.getProjectId(), ciPipelineImageDTO.getHarborRepoId(), ciPipelineImageDTO.getRepoType());

            // 设置拉取账户
            if (ciPipelineImageDTO.getRepoType().equals(CUSTOM_REPO)) {
                dockerDeployDTO.setDockerPullAccountDTO(new DockerPullAccountDTO(
                        harborRepoDTO.getHarborRepoConfig().getRepoUrl(),
                        harborRepoDTO.getHarborRepoConfig().getLoginName(),
                        harborRepoDTO.getHarborRepoConfig().getPassword()));
            } else {
                dockerDeployDTO.setDockerPullAccountDTO(new DockerPullAccountDTO(
                        harborRepoDTO.getHarborRepoConfig().getRepoUrl(),
                        harborRepoDTO.getPullRobot().getName(),
                        harborRepoDTO.getPullRobot().getToken()));
            }

            // 添加应用服务名用于部署记录  iamgeTag:172.23.xx.xx:30003/dev-25-test-25-4/go:2021.5.17-155211-master
            String imageTag = ciPipelineImageDTO.getImageTag();
            int indexOf = imageTag.lastIndexOf(":");
            String imageVersion = imageTag.substring(indexOf);
            String repoImageName = imageTag.substring(0, indexOf);

            image = ciPipelineImageDTO.getImageTag();
            deployVersion = imageVersion;
            deployObjectName = repoImageName.substring(repoImageName.lastIndexOf("/") + 1);
            AppServiceRepVO appServiceRepVO = applicationService.queryByCode(projectId, deployObjectName);
            appServiceId = appServiceRepVO == null ? null : appServiceRepVO.getId();
            serviceName = appServiceRepVO == null ? null : appServiceRepVO.getServiceName();
        }

        // 1. 更新状态 记录镜像信息
        // 2.保存记录
        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceService.queryByHostIdAndName(hostId, imageDeploy.getContainerName());

        if (devopsDockerInstanceDTO == null) {
            // 新建实例
            devopsDockerInstanceDTO = new DevopsDockerInstanceDTO(hostId,
                    imageDeploy.getContainerName(),
                    image,
                    DockerInstanceStatusEnum.OPERATING.value(),
                    AppSourceType.CURRENT_PROJECT.getValue());
            MapperUtil.resultJudgedInsertSelective(devopsDockerInstanceMapper, devopsDockerInstanceDTO, DevopsHostConstants.ERROR_SAVE_DOCKER_INSTANCE_FAILED);
            // 保存应用实例关系
            if (appServiceId != null) {
                devopsHostAppInstanceRelService.saveHostAppInstanceRel(projectId,
                        hostId,
                        appServiceId,
                        AppSourceType.CURRENT_PROJECT.getValue(),
                        devopsDockerInstanceDTO.getId(),
                        HostInstanceType.DOCKER_PROCESS.value(), serviceName);
            }
        } else {
            dockerDeployDTO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        }

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_DOCKER.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
        devopsHostCommandDTO.setCdJobRecordId(cdJobRecordId);
        devopsHostCommandDTO.setInstanceId(devopsDockerInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        dockerDeployDTO.setImage(image);
        dockerDeployDTO.setName(imageDeploy.getContainerName());
        try {
            dockerDeployDTO.setCmd(HostDeployUtil.genDockerRunCmd(dockerDeployDTO,
                    new String(decoder.decodeBuffer(imageDeploy.getValue()), StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new CommonException(e);
        }
        dockerDeployDTO.setInstanceId(String.valueOf(devopsDockerInstanceDTO.getId()));

        // 3. 保存部署记录
        devopsDeployRecordService.saveRecord(
                devopsCdPipelineRecordDTO.getProjectId(),
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
                new DeploySourceVO(AppSourceType.CURRENT_PROJECT, projectDTO.getName()));

        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());

        // 4. 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_DOCKER.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerDeployDTO));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>> deploy docker instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.DOCKER_INSTANCE, hostId, devopsDockerInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    private void updateStatusToSkip(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO, DevopsCdJobRecordDTO devopsCdJobRecordDTO) {
        Long cdJobRecordId = devopsCdJobRecordDTO.getId();
        Long projectId = devopsCdPipelineRecordDTO.getProjectId();
        Long pipelineRecordId = devopsCdPipelineRecordDTO.getId();
        Long stageRecordId = devopsCdJobRecordDTO.getStageRecordId();
        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SKIPPED.toValue());
        workFlowServiceOperator.approveUserTask(projectId, devopsCdPipelineRecordDTO.getBusinessKey(), MiscConstants.WORKFLOW_ADMIN_NAME, MiscConstants.WORKFLOW_ADMIN_ID, MiscConstants.WORKFLOW_ADMIN_ORG_ID);
        devopsCdPipelineService.setAppDeployStatus(pipelineRecordId, stageRecordId, cdJobRecordId, true);
    }


    @Override
    @Transactional
    public void retryHostDeployJob(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());
        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(cdJobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.IMAGED_DEPLOY.getValue())) {
            ApplicationContextHelper
                    .getSpringFactory()
                    .getBean(DevopsCdPipelineRecordService.class)
                    .pipelineDeployImage(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } else if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.JAR_DEPLOY.getValue())) {
            ApplicationContextHelper
                    .getSpringFactory()
                    .getBean(DevopsCdPipelineRecordService.class)
                    .pipelineDeployJar(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        }
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, "error.pipeline.id.is.null");
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
        devopsCdPipelineRecordDTO.setPipelineId(pipelineId);
        List<DevopsCdPipelineRecordDTO> devopsCdPipelineRecordDTOS = devopsCdPipelineRecordMapper.select(devopsCdPipelineRecordDTO);
        if (!CollectionUtils.isEmpty(devopsCdPipelineRecordDTOS)) {
            devopsCdPipelineRecordDTOS.forEach(cdPipelineRecordDTO -> devopsCdStageRecordService.deleteByPipelineRecordId(cdPipelineRecordDTO.getId()));
        }
        //删除cd 流水线记录
        devopsCdPipelineRecordMapper.delete(devopsCdPipelineRecordDTO);
    }

    @Override
    public DevopsCdPipelineRecordDTO queryById(Long id) {
        Assert.notNull(id, PipelineCheckConstant.ERROR_PIPELINE_RECORD_ID_IS_NULL);
        return devopsCdPipelineRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void updatePipelineStatusFailed(Long pipelineRecordId) {
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = queryById(pipelineRecordId);
        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.FAILED.toValue());
        update(devopsCdPipelineRecordDTO);
    }

    @Override
    @Transactional
    public void update(DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO) {
        devopsCdPipelineRecordDTO.setObjectVersionNumber(devopsCdPipelineRecordMapper.selectByPrimaryKey(devopsCdPipelineRecordDTO.getId()).getObjectVersionNumber());
        if (devopsCdPipelineRecordMapper.updateByPrimaryKeySelective(devopsCdPipelineRecordDTO) != 1) {
            throw new CommonException(ERROR_UPDATE_PIPELINE_RECORD_FAILED);
        }
    }

    @Override
    public Page<DevopsCdPipelineRecordVO> pagingCdPipelineRecord(Long projectId, Long pipelineId, PageRequest pageable) {
        Page<DevopsCdPipelineRecordVO> pipelineRecordInfo = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                () -> devopsCdPipelineRecordMapper.listByCiPipelineId(pipelineId));
        List<DevopsCdPipelineRecordVO> pipelineRecordVOList = pipelineRecordInfo.getContent();
        if (CollectionUtils.isEmpty(pipelineRecordVOList)) {
            return pipelineRecordInfo;
        }
        pipelineRecordVOList.forEach(devopsCdPipelineRecordVO -> {
            List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(devopsCdPipelineRecordVO.getId());
            if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
                //封装审核数据
                List<DevopsCdStageRecordVO> devopsCdStageRecordVOS = ConvertUtils.convertList(devopsCdStageRecordDTOS, DevopsCdStageRecordVO.class);
                for (DevopsCdStageRecordVO devopsCdStageRecordVO : devopsCdStageRecordVOS) {
                    //计算satge耗时
                    if (!CollectionUtils.isEmpty(devopsCdStageRecordVO.getJobRecordVOList())) {
                        Long seconds = devopsCdStageRecordVO.getJobRecordVOList().stream().filter(devopsCdJobRecordVO -> !Objects.isNull(devopsCdJobRecordVO.getDurationSeconds())).map(DevopsCdJobRecordVO::getDurationSeconds).reduce((aLong, aLong2) -> aLong + aLong2).get();
                        devopsCdStageRecordVO.setDurationSeconds(seconds);
                    }
                }
                // 计算流水线当前停留的审核节点
                addAuditStateInfo(devopsCdPipelineRecordVO);
                devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(devopsCdStageRecordVOS);
            } else {
                devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(Collections.emptyList());
            }
        });
        return pipelineRecordInfo;
    }

    private void addAuditStateInfo(DevopsCdPipelineRecordVO devopsCdPipelineRecordVO) {
        DevopsCdPipelineDeatilVO devopsCdPipelineDeatilVO = new DevopsCdPipelineDeatilVO();
        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryStageWithPipelineRecordIdAndStatus(devopsCdPipelineRecordVO.getId(), PipelineStatus.NOT_AUDIT.toValue());
        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
            DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordDTOS.get(0);
            // 继续判断阶段中是否还有待审核的任务
            List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryJobWithStageRecordIdAndStatus(devopsCdStageRecordDTO.getId(), PipelineStatus.NOT_AUDIT.toValue());
            if (!CollectionUtils.isEmpty(devopsCdJobRecordDTOS)) {
                DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordDTOS.get(0);
                DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = devopsCdAuditRecordService.queryByJobRecordIdAndUserId(devopsCdJobRecordDTO.getId(), DetailsHelper.getUserDetails().getUserId());
                devopsCdPipelineDeatilVO.setType("task");
                devopsCdPipelineDeatilVO.setStageName(devopsCdStageRecordDTO.getStageName());
                devopsCdPipelineDeatilVO.setExecute(devopsCdAuditRecordDTO != null && AuditStatusEnum.NOT_AUDIT.value().equals(devopsCdAuditRecordDTO.getStatus()));
                devopsCdPipelineDeatilVO.setStageRecordId(devopsCdStageRecordDTO.getId());
                devopsCdPipelineDeatilVO.setTaskRecordId(devopsCdJobRecordDTO.getId());
            }
        }

        devopsCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineDeatilVO);

    }

    @Override
    public DevopsCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long cdPipelineId) {
        if (Objects.isNull(cdPipelineId)) {
            return null;
        }
        DevopsCdPipelineRecordDTO cdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(cdPipelineId);
        if (Objects.isNull(cdPipelineRecordDTO)) {
            return null;
        }
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(cdPipelineRecordDTO.getCreatedBy());
        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = ConvertUtils.convertObject(cdPipelineRecordDTO, DevopsCdPipelineRecordVO.class);
        devopsCdPipelineRecordVO.setUsername(iamUserDTO.getRealName());
        devopsCdPipelineRecordVO.setCreatedDate(cdPipelineRecordDTO.getCreationDate());
        CiCdPipelineDTO ciCdPipelineDTO = devopsCiCdPipelineMapper.selectByPrimaryKey(cdPipelineRecordDTO.getPipelineId());
        if (Objects.isNull(ciCdPipelineDTO)) {
            return null;
        }
        AppServiceDTO serviceDTO = appServiceMapper.selectByPrimaryKey(ciCdPipelineDTO.getAppServiceId());
        devopsCdPipelineRecordVO.setGitlabProjectId(serviceDTO.getGitlabProjectId());
        //查询流水线信息
        CiCdPipelineVO ciCdPipelineVO = devopsCiCdPipelineMapper.queryById(cdPipelineRecordDTO.getPipelineId());
        //添加提交信息
        devopsCdPipelineRecordVO.setCiCdPipelineVO(ciCdPipelineVO);
        addCommitInfo(ciCdPipelineVO.getAppServiceId(), devopsCdPipelineRecordVO, cdPipelineRecordDTO);

        devopsCdPipelineRecordVO.setCiCdPipelineVO(ciCdPipelineVO);
        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(devopsCdPipelineRecordVO.getId());
        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
            List<DevopsCdStageRecordVO> devopsCdStageRecordVOS = ConvertUtils.convertList(devopsCdStageRecordDTOS, this::dtoToVo);
            devopsCdStageRecordVOS.sort(Comparator.comparing(StageRecordVO::getSequence));
            devopsCdStageRecordVOS.forEach(devopsCdStageRecordVO -> {
                devopsCdStageRecordVO.setType(StageType.CD.getType());
                //查询Cd job
                List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(devopsCdStageRecordVO.getId());
                List<DevopsCdJobRecordVO> devopsCdJobRecordVOS = ConvertUtils.convertList(devopsCdJobRecordDTOS, DevopsCdJobRecordVO.class);

                // 根据job类型，添加相关信息
                calculateJob(devopsCdJobRecordVOS);

                devopsCdJobRecordVOS.sort(Comparator.comparing(DevopsCdJobRecordVO::getSequence));
                devopsCdStageRecordVO.setJobRecordVOList(devopsCdJobRecordVOS);

                //计算stage耗时
                List<Long> collect = devopsCdJobRecordVOS.stream().filter(devopsCdJobRecordVO -> !Objects.isNull(devopsCdJobRecordVO.getDurationSeconds())).map(DevopsCdJobRecordVO::getDurationSeconds).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect))
                    devopsCdStageRecordVO.setDurationSeconds(collect.stream().reduce((aLong, aLong2) -> aLong + aLong2).get());
            });
            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(devopsCdStageRecordVOS);
        } else {
            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(Collections.emptyList());
        }
        // 计算流水线当前停留的审核节点
        if (PipelineStatus.NOT_AUDIT.toValue().equals(devopsCdPipelineRecordVO.getStatus())) {
            addAuditStateInfo(devopsCdPipelineRecordVO);
        }

        return devopsCdPipelineRecordVO;
    }


    private void addCommitInfo(Long appServiceId, DevopsCdPipelineRecordVO devopsCPipelineRecordVO, DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO) {
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(devopsCdPipelineRecordDTO.getCommitSha(), devopsCdPipelineRecordDTO.getRef());

        CustomCommitVO customCommitVO = new CustomCommitVO();
        devopsCPipelineRecordVO.setCommit(customCommitVO);

        customCommitVO.setGitlabProjectUrl(applicationService.calculateGitlabProjectUrlWithSuffix(appServiceId));

        // 可能因为GitLab webhook 失败, commit信息查不出
        if (devopsGitlabCommitDTO == null) {
            return;
        }
        IamUserDTO commitUser = null;
        if (devopsGitlabCommitDTO.getUserId() != null) {
            commitUser = baseServiceClientOperator.queryUserByUserId(devopsGitlabCommitDTO.getUserId());
        }

        customCommitVO.setRef(devopsCdPipelineRecordDTO.getRef());
        customCommitVO.setCommitSha(devopsCdPipelineRecordDTO.getCommitSha());
        customCommitVO.setCommitContent(devopsGitlabCommitDTO.getCommitContent());
        customCommitVO.setCommitUrl(devopsGitlabCommitDTO.getUrl());

        if (commitUser != null) {
            customCommitVO.setUserHeadUrl(commitUser.getImageUrl());
            customCommitVO.setUserName(commitUser.getLdap() ? commitUser.getLoginName() : commitUser.getEmail());
        }
    }

    private void calculateJob(List<DevopsCdJobRecordVO> devopsCdJobRecordVOS) {
        devopsCdJobRecordVOS.forEach(devopsCdJobRecordVO -> {
            //如果是自动部署返回 能点击查看生成实例的相关信息
            if (JobTypeEnum.CD_DEPLOY.value().equals(devopsCdJobRecordVO.getType())) {
                //部署环境 应用服务 生成版本 实例名称
                Long commandId = devopsCdJobRecordVO.getCommandId();
                if (commandId != null) {
                    DeployRecordVO deployRecordVO = devopsDeployRecordService.queryEnvDeployRecordByCommandId(commandId);
                    if (deployRecordVO != null) {
                        DevopsCdJobRecordVO.CdAuto cdAuto = devopsCdJobRecordVO.new CdAuto();
                        cdAuto.setAppServiceId(deployRecordVO.getAppServiceId());
                        cdAuto.setAppServiceName(deployRecordVO.getDeployObjectName());
                        cdAuto.setAppServiceVersion(deployRecordVO.getDeployObjectVersion());
                        cdAuto.setEnvId(deployRecordVO.getEnvId());
                        cdAuto.setEnvName(deployRecordVO.getDeployPayloadName());
                        cdAuto.setInstanceId(deployRecordVO.getInstanceId());
                        cdAuto.setInstanceName(deployRecordVO.getInstanceName());

                        devopsCdJobRecordVO.setCdAuto(cdAuto);
                    }
                }
            }
            //如果是人工审核返回审核信息
            if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobRecordVO.getType())) {
                // 指定审核人员 已审核人员 审核状态
                DevopsCdJobRecordVO.Audit audit = devopsCdJobRecordVO.new Audit();
                List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, null, devopsCdJobRecordVO.getJobId());
                if (!CollectionUtils.isEmpty(devopsCdAuditDTOS)) {
                    List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(devopsCdAuditDTOS.stream().map(DevopsCdAuditDTO::getUserId).collect(Collectors.toList()));
                    audit.setAppointUsers(iamUserDTOS);
                }
                List<DevopsCdAuditRecordDTO> devopsCdAuditRecordDTOS = devopsCdAuditRecordService.queryByJobRecordId(devopsCdJobRecordVO.getId());
                if (!CollectionUtils.isEmpty(devopsCdAuditRecordDTOS)) {
                    List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(devopsCdAuditRecordDTOS.stream()
                            .filter(devopsCdAuditRecordDTO -> AuditStatusEnum.PASSED.value().equals(devopsCdAuditRecordDTO.getStatus()) || AuditStatusEnum.REFUSED.value().equals(devopsCdAuditRecordDTO.getStatus()))
                            .map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList()));
                    audit.setReviewedUsers(iamUserDTOS);
                    audit.setStatus(devopsCdJobRecordVO.getStatus());
                }
                devopsCdJobRecordVO.setAudit(audit);
            }
            //如果是主机部署 显示主机部署模式(镜像，jar，自定义)，来源，关联构建任务
            if (JobTypeEnum.CD_HOST.value().equals(devopsCdJobRecordVO.getType())) {
                CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(devopsCdJobRecordVO.getMetadata(), CdHostDeployConfigVO.class);
                devopsCdJobRecordVO.setCdHostDeployConfigVO(cdHostDeployConfigVO);
            }

            if (JobTypeEnum.CD_API_TEST.value().equals(devopsCdJobRecordVO.getType())
                    && devopsCdJobRecordVO.getApiTestTaskRecordId() != null) {
                try {
                    ApiTestTaskRecordVO apiTestTaskRecordVO = testServiceClientoperator.queryById(devopsCdJobRecordVO.getProjectId(), devopsCdJobRecordVO.getApiTestTaskRecordId());
                    CdApiTestConfigVO cdApiTestConfigVO = gson.fromJson(devopsCdJobRecordVO.getMetadata(), CdApiTestConfigVO.class);
                    apiTestTaskRecordVO.setDeployJobName(cdApiTestConfigVO.getDeployJobName());
                    apiTestTaskRecordVO.setPerformThreshold(cdApiTestConfigVO.getWarningSettingVO().getPerformThreshold());
                    devopsCdJobRecordVO.setApiTestTaskRecordVO(apiTestTaskRecordVO);
                } catch (Exception ex) {
                    LOGGER.warn("Failed to query api test task record..., the ex code is {}", ex.getMessage());
                }
            }

            if (JobTypeEnum.CD_EXTERNAL_APPROVAL.value().equals(devopsCdJobRecordVO.getType())) {
                ExternalApprovalJobVO externalApprovalJobVO = gson.fromJson(devopsCdJobRecordVO.getMetadata(), ExternalApprovalJobVO.class);
                devopsCdJobRecordVO.setExternalApprovalJobVO(externalApprovalJobVO);
            }
        });

    }

    @Override
    public Boolean testConnection(HostConnectionVO hostConnectionVO) {
        SSHClient ssh = new SSHClient();
        Session session = null;
        Boolean index = true;
        try {
            sshUtil.sshConnect(hostConnectionVO, ssh);
            session = ssh.startSession();
            Session.Command cmd = session.exec("echo Hello World!!!");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(IOUtils.readFully(cmd.getInputStream()).toString());
            }
            cmd.join(5, TimeUnit.SECONDS);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("** exit status: {}", cmd.getExitStatus());
            }
            if (cmd.getExitStatus() != 0) {
                throw new CommonException("error.test.connection");
            }
        } catch (IOException e) {
            index = false;
            LOGGER.error("error ssh connect", e);
        } finally {
            closeSsh(ssh, session);
        }
        return index;
    }

    private DevopsCdStageRecordVO dtoToVo(DevopsCdStageRecordDTO devopsCdStageRecordDTO) {
        DevopsCdStageRecordVO devopsCdStageRecordVO = new DevopsCdStageRecordVO();
        BeanUtils.copyProperties(devopsCdStageRecordDTO, devopsCdStageRecordVO);
        devopsCdStageRecordVO.setName(devopsCdStageRecordDTO.getStageName());
        return devopsCdStageRecordVO;
    }

    @Override
    public DevopsCdPipelineRecordVO queryByCdPipelineRecordId(Long cdPipelineRecordId) {
        if (cdPipelineRecordId == null || cdPipelineRecordId == 0L) {
            return null;
        }
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(cdPipelineRecordId);
        if (Objects.isNull(devopsCdPipelineRecordDTO)) {
            return null;
        }
        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = new DevopsCdPipelineRecordVO();
        BeanUtils.copyProperties(devopsCdPipelineRecordDTO, devopsCdPipelineRecordVO);
        devopsCdPipelineRecordVO.setCreatedDate(devopsCdPipelineRecordDTO.getCreationDate());
        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(devopsCdPipelineRecordVO.getId());
        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
            //封装审核数据
            List<DevopsCdStageRecordVO> devopsCdStageRecordVOS = ConvertUtils.convertList(devopsCdStageRecordDTOS, DevopsCdStageRecordVO.class);
            devopsCdStageRecordVOS.sort(Comparator.comparing(StageRecordVO::getSequence));
            // 计算流水线当前停留的审核节点
            if (PipelineStatus.NOT_AUDIT.toValue().equals(devopsCdPipelineRecordDTO.getStatus())) {
                addAuditStateInfo(devopsCdPipelineRecordVO);
            }
            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(devopsCdStageRecordVOS);
        } else {
            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(Collections.emptyList());
        }

        return devopsCdPipelineRecordVO;
    }

}
