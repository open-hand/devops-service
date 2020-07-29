package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_HOST_FEPLOY;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoImageTagVo;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.HostDeployPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nImageDeployDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusDeployDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineStageDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineTaskDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
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
    private static final String ERROR_DOCKER_LOGIN = "error.docker.login";
    private static final String ERROR_DOCKER_PULL = "error.docker.pull";
    private static final String ERROR_DOCKER_RUN = "error.docker.run";
    private static final String ERROR_DOWNLOAD_JAY = "error.download.jar";
    private static final String ERROR_JAVA_JAR = "error.java.jar";
    private static final String UNAUTHORIZED = "unauthorized";
    private static final String STAGE = "stage";
    private static final String TASK = "task";

    public static final Logger LOGGER = LoggerFactory.getLogger(DevopsCdPipelineRecordServiceImpl.class);

    private static final Gson gson = new Gson();

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
    private DevopsEnvironmentMapper devopsEnvironmentMapper;

    @Autowired
    private AppServiceMapper appServiceMapper;

    @Autowired
    private AppServiceVersionMapper appServiceVersionMapper;

    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;

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

    @Value("${choerodon.online:true}")
    private Boolean online;

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

        for (int i = 0; i < stageRecordDTOList.size(); i++) {
            // 3.
            DevopsPipelineStageDTO stageDTO = new DevopsPipelineStageDTO();
            DevopsCdStageRecordDTO stageRecordDTO = stageRecordDTOList.get(i);
            stageDTO.setStageRecordId(stageRecordDTO.getId());
            // 4.
            List<DevopsCdJobRecordDTO> jobRecordDTOList;
            if (!isRetry || i > 0) {
                jobRecordDTOList = devopsCdJobRecordService.queryByStageRecordId(stageRecordDTO.getId());
            } else {
                jobRecordDTOList = devopsCdJobRecordMapper.queryRetryJob(stageRecordDTO.getId());
            }
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
                stageDTO.setNextStageTriggerType(stageRecordDTOList.get(i + 1).getTriggerType());
                if (stageRecordDTOList.get(i + 1).getTriggerType().equals(DeployType.MANUAL.getType())) {
                    List<DevopsCdAuditRecordDTO> stageAuditRecordDTOS = devopsCdAuditRecordService.queryByStageRecordId(stageRecordDTOList.get(i + 1).getId());
                    if (CollectionUtils.isEmpty(stageAuditRecordDTOS)) {
                        throw new CommonException("error.audit.stage.noUser");
                    }
                    List<String> users = stageAuditRecordDTOS.stream().map(t -> TypeUtil.objToString(t.getUserId())).collect(Collectors.toList());
                    stageDTO.setUsernames(users);
                    stageDTO.setMultiAssign(users.size() > 1);
                }
            }
            // ci cd 间的审核任务 放在流水线
            if (!isRetry && i == 0) {
                if (stageRecordDTO.getTriggerType().equals(DeployType.MANUAL.getType())) {
                    List<DevopsCdAuditRecordDTO> stageAuditRecordDTOS = devopsCdAuditRecordService.queryByStageRecordId(stageRecordDTO.getId());
                    if (CollectionUtils.isEmpty(stageAuditRecordDTOS)) {
                        throw new CommonException("error.audit.stage.noUser");
                    }
                    List<String> users = stageAuditRecordDTOS.stream().map(t -> TypeUtil.objToString(t.getUserId())).collect(Collectors.toList());
                    devopsPipelineDTO.setUserNames(users);
                    devopsPipelineDTO.setMultiAssign(users.size() > 1);
                }
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

    @Override
    public Boolean cdHostImageDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        LOGGER.info("========================================");
        LOGGER.info("start image deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
        Boolean status = true;
        SSHClient ssh = new SSHClient();
        try {
            // 0.1
            DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
            CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
            CdHostDeployConfigVO.ImageDeploy imageDeploy = cdHostDeployConfigVO.getImageDeploy();
            imageDeploy.setValue(new String(decoder.decodeBuffer(imageDeploy.getValue()), "UTF-8"));
            // 0.2
            C7nImageDeployDTO c7nImageDeployDTO = new C7nImageDeployDTO();
            if (imageDeploy.getDeploySource().equals(HostDeploySource.MATCH_DEPLOY.getValue())) {
                HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(imageDeploy.getRepoType(), TypeUtil.objToLong(imageDeploy.getRepoId()), imageDeploy.getImageName());
                List<HarborC7nImageTagVo> filterImageTagVoList;
                if (CollectionUtils.isEmpty(imageTagVo.getImageTagList())) {
                    devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SKIPPED.toValue());
                    LOGGER.info("no image to deploy,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
                    return status;
                } else {
                    String pattern = getRegexStr(imageDeploy);
                    filterImageTagVoList = imageTagVo.getImageTagList().stream().filter(t -> Pattern.matches(pattern, t.getTagName())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(filterImageTagVoList)) {
                        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SKIPPED.toValue());
                        LOGGER.info("no image to deploy,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
                        return status;
                    }
                }
                BeanUtils.copyProperties(imageTagVo, c7nImageDeployDTO);
                c7nImageDeployDTO.setPullCmd(filterImageTagVoList.get(0).getPullCmd());
            } else {
                DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
                if (ObjectUtils.isEmpty(devopsCdPipelineRecordDTO.getGitlabPipelineId())) {
                    throw new CommonException("error.no.gitlab.pipeline.id");
                }
                CiPipelineImageDTO ciPipelineImageDTO = ciPipelineImageService.queryByGitlabPipelineId(devopsCdPipelineRecordDTO.getGitlabPipelineId(), imageDeploy.getPipelineTask());
                HarborRepoDTO harborRepoDTO = rdupmClientOperator.queryHarborRepoConfigById(devopsCdPipelineRecordDTO.getProjectId(), ciPipelineImageDTO.getHarborRepoId(), ciPipelineImageDTO.getRepoType());
                c7nImageDeployDTO.setHarborUrl(harborRepoDTO.getHarborRepoConfig().getRepoUrl());
                c7nImageDeployDTO.setPullAccount(harborRepoDTO.getPullRobot().getName());
                c7nImageDeployDTO.setPullPassword(harborRepoDTO.getPullRobot().getToken());
                c7nImageDeployDTO.setPullCmd("docker pull " + ciPipelineImageDTO.getImageTag());
            }
            // 1. 更新状态 记录镜像信息
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());
            jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
            jobRecordDTO.setDeployMetadata(gson.toJson(c7nImageDeployDTO));
            devopsCdJobRecordService.update(jobRecordDTO);
            // 2.
            sshConnect(cdHostDeployConfigVO.getHostConnectionVO(), ssh);
            // 3.
            // 3.1
            dockerLogin(ssh, c7nImageDeployDTO);
            // 3.2
            dockerPull(ssh, c7nImageDeployDTO);

            dockerStop(ssh, imageDeploy);
            // 3.3
            dockerRun(ssh, imageDeploy, c7nImageDeployDTO);
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SUCCESS.toValue());
            LOGGER.info("========================================");
            LOGGER.info("image deploy cd host job success!!!,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
            jobFailed(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } finally {
            closeSsh(ssh, null);
        }
        return status;
    }

    private void dockerLogin(SSHClient ssh, C7nImageDeployDTO imageTagVo) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();
//            String loginExec = String.format("docker login -u %s -p %s %s", imageTagVo.getPullAccount(), imageTagVo.getPullPassword(), imageTagVo.getHarborUrl());
            String loginExec = String.format("docker login -u %s -p %s %s", "admin", "Handhand123", imageTagVo.getHarborUrl());
            LOGGER.info(loginExec);
            Session.Command cmd = session.exec(loginExec);

            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();

            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
            LOGGER.info("docker login status:{}", cmd.getExitStatus());

            if (cmd.getExitStatus() != 0) {
                throw new CommonException(ERROR_DOCKER_LOGIN);
            }

        } finally {
            assert session != null;
            session.close();
        }
    }

    private void dockerPull(SSHClient ssh, C7nImageDeployDTO imageTagVo) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();
            LOGGER.info(imageTagVo.getPullCmd());
            Session.Command cmd = session.exec(imageTagVo.getPullCmd());
            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();
            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
            LOGGER.info("docker pull status:{}", cmd.getExitStatus());
            if (cmd.getExitStatus() != 0) {
                throw new CommonException(ERROR_DOCKER_PULL);
            }
        } finally {
            assert session != null;
            session.close();
        }
    }

    private void dockerRun(SSHClient ssh, CdHostDeployConfigVO.ImageDeploy imageDeploy, C7nImageDeployDTO c7nImageDeployDTO) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();
            String[] strings = imageDeploy.getValue().split("\n");
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
            StringBuilder dockerRunExec = new StringBuilder();
            dockerRunExec.append(values.replace("${containerName}", imageDeploy.getContainerName()).replace("${imageName}", c7nImageDeployDTO.getPullCmd().replace("docker pull", "")));
            LOGGER.info(dockerRunExec.toString());
            Session.Command cmd = session.exec(dockerRunExec.toString());
            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();
            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
            LOGGER.info("docker run status:{}", cmd.getExitStatus());
            if (cmd.getExitStatus() != 0) {
                throw new CommonException(ERROR_DOCKER_RUN);
            }
        } finally {
            assert session != null;
            session.close();
        }

    }

    private void dockerStop(SSHClient ssh, CdHostDeployConfigVO.ImageDeploy imageDeploy) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();

            // 判断镜像是否存在 存在删除 部署
            StringBuilder dockerRunExec = new StringBuilder();
            dockerRunExec.append("docker stop ").append(imageDeploy.getContainerName()).append(" && ");
            dockerRunExec.append("docker rm ").append(imageDeploy.getContainerName());
            LOGGER.info(dockerRunExec.toString());
            Session.Command cmd = session.exec(dockerRunExec.toString());
            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();
            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
            LOGGER.info("docker stop status:{}", cmd.getExitStatus());
        } finally {
            assert session != null;
            session.close();
        }

    }

    private void sshConnect(HostConnectionVO hostConnectionVO, SSHClient ssh) throws IOException {
        // 3.
//        ssh.loadKnownHosts();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(hostConnectionVO.getHostIp(), TypeUtil.objToInteger(hostConnectionVO.getHostPort()));
        if (hostConnectionVO.getAccountType().equals(CdHostAccountType.PASSWORD.value())) {
            ssh.authPassword(hostConnectionVO.getUserName(), hostConnectionVO.getPassword());
        } else {
            KeyProvider keyProvider = ssh.loadKeys(hostConnectionVO.getPassword(), null, null);
            ssh.authPublickey(hostConnectionVO.getUserName(), keyProvider);
        }
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

    @Override
    public Boolean cdHostJarDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        LOGGER.info("========================================");
        LOGGER.info("start jar deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
        SSHClient ssh = new SSHClient();
        Boolean status = true;
        try {
            // 0.1 查询部署信息
            DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
            CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
            CdHostDeployConfigVO.JarDeploy jarDeploy = cdHostDeployConfigVO.getJarDeploy();
            jarDeploy.setValue(new String(decoder.decodeBuffer(jarDeploy.getValue()), "UTF-8"));
            C7nNexusDeployDTO c7nNexusDeployDTO = new C7nNexusDeployDTO();
            DevopsCdPipelineRecordDTO cdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(cdPipelineRecordDTO.getProjectId());

            // 0.2 从制品库获取仓库信息
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
                versionRegular = "^" + ciPipelineMavenDTO.getVersion() + "$";
            }

            // 0.3 获取并记录信息
            List<C7nNexusComponentDTO> nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), cdPipelineRecordDTO.getProjectId(), nexusRepoId, groupId, artifactId, versionRegular);
            if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
                devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SKIPPED.toValue());
                LOGGER.info("no jar to deploy,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
                return true;
            }
            List<NexusMavenRepoDTO> mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), cdPipelineRecordDTO.getProjectId(), Collections.singleton(nexusRepoId));
            if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
                throw new CommonException("error.get.maven.config");
            }
            c7nNexusDeployDTO.setPullUserId(mavenRepoDTOList.get(0).getNePullUserId());
            c7nNexusDeployDTO.setPullUserPassword(mavenRepoDTOList.get(0).getNePullUserPassword());
            c7nNexusDeployDTO.setDownloadUrl(nexusComponentDTOList.get(0).getDownloadUrl());

            // 1.更新流水线状态 记录信息
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());
            c7nNexusDeployDTO.setJarName(getJarName(c7nNexusDeployDTO.getDownloadUrl()));
            jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
            jobRecordDTO.setDeployMetadata(gson.toJson(c7nNexusDeployDTO));
            devopsCdJobRecordService.update(jobRecordDTO);

            sshConnect(cdHostDeployConfigVO.getHostConnectionVO(), ssh);

            // 2. 执行jar部署
            sshStopJar(ssh, jobRecordDTO.getJobId());
            sshExec(ssh, c7nNexusDeployDTO, jarDeploy);
            devopsCdEnvDeployInfoService.updateOrUpdateByCdJob(jobRecordDTO.getJobId(), c7nNexusDeployDTO.getJarName());
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SUCCESS.toValue());
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
            jobFailed(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } finally {
            closeSsh(ssh, null);
        }
        return status;
    }

    @Override
    public Boolean cdHostCustomDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        LOGGER.info("========================================");
        LOGGER.info("start custom deploy cd host job,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
        SSHClient ssh = new SSHClient();
        Boolean status = true;
        try {
            // 0.1 查询部署信息
            DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
            CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
            String value = new String(decoder.decodeBuffer(cdHostDeployConfigVO.getCustomize().getValues()), "UTF-8");
            sshConnect(cdHostDeployConfigVO.getHostConnectionVO(), ssh);
            sshExecCustom(ssh, value);
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SUCCESS.toValue());
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
            jobFailed(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } finally {
            closeSsh(ssh, null);
        }
        return status;
    }

    private void sshStopJar(SSHClient ssh, Long jobId) throws IOException {
        DevopsCdEnvDeployInfoDTO cdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryByCdJobId(jobId);
        if (cdEnvDeployInfoDTO != null && !StringUtils.isEmpty(cdEnvDeployInfoDTO.getJarName())) {
            StringBuilder stopJar = new StringBuilder();
            stopJar.append(String.format("ps aux|grep %s | grep -v grep |awk '{print  $2}' |xargs kill -9 ", cdEnvDeployInfoDTO.getJarName()));
            stopJar.append(System.lineSeparator());
            stopJar.append(String.format("cd temp-jar && rm -f %s", cdEnvDeployInfoDTO.getJarName()));
            stopJar.append(" && cd ../ && cd temp-log &&");
            stopJar.append(String.format("rm -f %s", cdEnvDeployInfoDTO.getJarName().replace(".jar", ".log")));
            LOGGER.info(stopJar.toString());
            Session session = null;
            try {
                session = ssh.startSession();
                final Session.Command cmd = session.exec(stopJar.toString());
                LOGGER.info(IOUtils.readFully(cmd.getInputStream()).toString());
                LOGGER.info(IOUtils.readFully(cmd.getErrorStream()).toString());
            } finally {
                assert session != null;
                session.close();
            }
        }
    }

    private void sshExec(SSHClient ssh, C7nNexusDeployDTO c7nNexusDeployDTO, CdHostDeployConfigVO.JarDeploy jarDeploy) throws IOException {
        StringBuilder cmdStr = new StringBuilder();
        cmdStr.append("mkdir temp-jar ").append(System.lineSeparator());
        cmdStr.append("mkdir temp-log ").append(System.lineSeparator());
        cmdStr.append("cd temp-jar ").append(System.lineSeparator());
        Session session = null;
        try {
            session = ssh.startSession();
            // 2.2
            String curlExec = String.format("curl -o %s -u %s:%s %s ",
                    c7nNexusDeployDTO.getJarName(),
                    c7nNexusDeployDTO.getPullUserId(),
                    c7nNexusDeployDTO.getPullUserPassword(),
                    c7nNexusDeployDTO.getDownloadUrl());
            cmdStr.append(curlExec).append(System.lineSeparator());

            // 2.3
            String[] strings = jarDeploy.getValue().split("\n");
            String values = "";
            for (String s : strings) {
                if (s.length() > 0 && !s.contains("#") && s.contains("java")) {
                    values = s;
                }
            }
            if (StringUtils.isEmpty(values) || !checkInstruction("jar", values)) {
                throw new CommonException("error.instruction");
            }

//        String javaJarExec = String.format("echo `nohup %s > sss.log & `", values.replace("${jar}", jarName));
            String logName = c7nNexusDeployDTO.getJarName().replace(".jar", ".log");
            String javaJarExec = String.format("nohup %s > ~/temp-log/%s & ", values.replace("${jar}", c7nNexusDeployDTO.getJarName()), logName);

            cmdStr.append(javaJarExec);
            LOGGER.info(cmdStr.toString());

            final Session.Command cmd = session.exec(cmdStr.toString());
            cmd.join(5, TimeUnit.SECONDS);
            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();

            if (loggerError.contains("Unauthorized") || loggerInfo.contains("Unauthorized")) {
                throw new CommonException(ERROR_DOWNLOAD_JAY);
            }
            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
        } finally {
            assert session != null;
            session.close();
        }

    }

    private void sshExecCustom(SSHClient ssh, String value) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();
            String[] strings = value.split("\n");
            String values = "";
            for (String s : strings) {
                if (s.length() > 0 && !s.contains("#")) {
                    values = s;
                }
            }
            if (StringUtils.isEmpty(values)) {
                throw new CommonException("error.instruction");
            }

            LOGGER.info(values);
            final Session.Command cmd = session.exec(values);
            cmd.join(5, TimeUnit.SECONDS);
            String loggerInfo = IOUtils.readFully(cmd.getInputStream()).toString();
            String loggerError = IOUtils.readFully(cmd.getErrorStream()).toString();

            LOGGER.info(loggerInfo);
            LOGGER.info(loggerError);
        } finally {
            assert session != null;
            session.close();
        }

    }

    private void closeSsh(SSHClient ssh, Session session) {
        try {
            if (session != null) {
                session.close();
            }
            ssh.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getJarName(String url) {
        String[] arr = url.split("/");
        return arr[arr.length - 1].replace(".jar", "-") + GenerateUUID.generateRandomString() + ".jar";
    }

    private void jobFailed(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.FAILED.toValue());
        devopsCdStageRecordService.updateStatusById(cdStageRecordId, PipelineStatus.FAILED.toValue());
        updateStatusById(pipelineRecordId, PipelineStatus.FAILED.toValue());
    }

    @Override
    @Saga(code = DEVOPS_HOST_FEPLOY,
            description = "devops主机部署", inputSchema = "{}")
    public void cdHostDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        HostDeployPayload hostDeployPayload = new HostDeployPayload(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        DevopsCdPipelineRecordDTO pipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
        CustomContextUtil.setUserContext(pipelineRecordDTO.getCreatedBy());
        if (online) {

            producer.apply(
                    StartSagaBuilder
                            .newBuilder()
                            .withLevel(ResourceLevel.PROJECT)
                            .withSourceId(pipelineRecordDTO.getProjectId())
                            .withRefType("pipelineRecordId")
                            .withSagaCode(SagaTopicCodeConstants.DEVOPS_HOST_FEPLOY),
                    builder -> builder
                            .withPayloadAndSerialize(hostDeployPayload)
                            .withRefId(pipelineRecordId.toString()));
        } else {
            DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(hostDeployPayload.getJobRecordId());
            CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
            if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.IMAGED_DEPLOY.getValue())) {
                cdHostImageDeploy(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId());
            } else if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.JAR_DEPLOY.getValue())) {
                cdHostJarDeploy(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId());
            } else {
                cdHostCustomDeploy(hostDeployPayload.getPipelineRecordId(), hostDeployPayload.getStageRecordId(), hostDeployPayload.getJobRecordId());
            }
        }
    }

    @Override
    @Transactional
    public void retryHostDeployJob(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());
        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(cdJobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.IMAGED_DEPLOY.getValue())) {
            retryHostImageDeploy(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } else if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.JAR_DEPLOY.getValue())) {
            retryHostJarDeploy(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        }
        devopsCdStageRecordService.updateStatusById(cdStageRecordId, PipelineStatus.SUCCESS.toValue());
        updateStatusById(pipelineRecordId, PipelineStatus.SUCCESS.toValue());
    }

    private void retryHostImageDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());
        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(cdJobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        CdHostDeployConfigVO.ImageDeploy imageDeploy = cdHostDeployConfigVO.getImageDeploy();
        C7nImageDeployDTO imageTagVoRecord = gson.fromJson(cdJobRecordDTO.getDeployMetadata(), C7nImageDeployDTO.class);
        SSHClient ssh = new SSHClient();
        try {
            sshConnect(cdHostDeployConfigVO.getHostConnectionVO(), ssh);
            dockerLogin(ssh, imageTagVoRecord);
            dockerPull(ssh, imageTagVoRecord);
            dockerStop(ssh, imageDeploy);
            dockerRun(ssh, imageDeploy, imageTagVoRecord);

            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SUCCESS.toValue());
        } catch (Exception e) {
            jobFailed(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } finally {
            closeSsh(ssh, null);
        }
    }


    private void retryHostJarDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());
        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(cdJobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        C7nNexusDeployDTO c7nNexusDeployDTO = gson.fromJson(cdJobRecordDTO.getDeployMetadata(), C7nNexusDeployDTO.class);
        SSHClient ssh = new SSHClient();
        try {
            sshConnect(cdHostDeployConfigVO.getHostConnectionVO(), ssh);
            // 2.1
            sshStopJar(ssh, cdJobRecordDTO.getJobId());
            sshExec(ssh, c7nNexusDeployDTO, cdHostDeployConfigVO.getJarDeploy());
            devopsCdEnvDeployInfoService.updateOrUpdateByCdJob(cdJobRecordDTO.getJobId(), c7nNexusDeployDTO.getJarName());
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SUCCESS.toValue());
        } catch (Exception e) {
            jobFailed(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } finally {
            closeSsh(ssh, null);
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
            devopsCdPipelineRecordDTOS.forEach(cdPipelineRecordDTO -> {
                devopsCdStageRecordService.deleteByPipelineRecordId(cdPipelineRecordDTO.getId());
            });
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
    public void updatePipelineStatusFailed(Long pipelineRecordId, String errorInfo) {
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = queryById(pipelineRecordId);
        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.FAILED.toValue());
//        devopsCdPipelineRecordDTO.setErrorInfo(errorInfo);
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
                    //查询Cd job
                    List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(devopsCdStageRecordVO.getId());
                    List<DevopsCdJobRecordVO> devopsCdJobRecordVOS = ConvertUtils.convertList(devopsCdJobRecordDTOS, DevopsCdJobRecordVO.class);
                    //计算job耗时
                    devopsCdJobRecordVOS.forEach(devopsCdJobRecordVO -> {
                        devopsCdJobRecordVO.setJobExecuteTime();
                    });
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
                devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(Collections.EMPTY_LIST);
            }
        });
        return pipelineRecordInfo;
    }

    private void addAuditStateInfo(DevopsCdPipelineRecordVO devopsCdPipelineRecordVO) {
        DevopsCdPipelineDeatilVO devopsCdPipelineDeatilVO = new DevopsCdPipelineDeatilVO();
        DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordService.queryStageWithPipelineRecordIdAndStatus(devopsCdPipelineRecordVO.getId(), PipelineStatus.NOT_AUDIT.toValue());
        if (devopsCdStageRecordDTO != null) {
            // 继续判断阶段中是否还有待审核的任务
            DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordService.queryJobWithStageRecordIdAndStatus(devopsCdStageRecordDTO.getId(), PipelineStatus.NOT_AUDIT.toValue());
            if (devopsCdJobRecordDTO == null) {
                DevopsCdAuditRecordDTO devopsCdAuditRecordDTO = devopsCdAuditRecordService.queryByStageRecordIdAndUserId(devopsCdStageRecordDTO.getId(), DetailsHelper.getUserDetails().getUserId());
                devopsCdPipelineDeatilVO.setType("stage");
                devopsCdPipelineDeatilVO.setStageName(devopsCdStageRecordDTO.getStageName());
                devopsCdPipelineDeatilVO.setExecute(devopsCdAuditRecordDTO != null && AuditStatusEnum.NOT_AUDIT.value().equals(devopsCdAuditRecordDTO.getStatus()));
                devopsCdPipelineDeatilVO.setStageRecordId(devopsCdStageRecordDTO.getId());
            } else {
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

    private DevopsCdPipelineDeatilVO generateCdPipelineDeatilVO(DevopsCdStageRecordVO devopsCdStageRecordVO) {
        //判断阶段是不是在审核中，如果阶段在审核中判断阶段的job是不是处于审核中
        DevopsCdStageDTO cdStageDTO = devopsCdStageMapper.selectByPrimaryKey(devopsCdStageRecordVO.getStageId());
        if (Objects.isNull(cdStageDTO)) {
            return null;
        }
        DevopsCdPipelineDeatilVO devopsCdPipelineDeatilVO = null;
        //阶段待审核状态
        if (AuditStatusEnum.NOT_AUDIT.value().equals(devopsCdStageRecordVO.getStatus())) {
            devopsCdPipelineDeatilVO = new DevopsCdPipelineDeatilVO();
            devopsCdPipelineDeatilVO.setStageName(cdStageDTO.getName());
            devopsCdPipelineDeatilVO.setStageRecordId(devopsCdStageRecordVO.getId());
            devopsCdPipelineDeatilVO.setExecute(executeValue(cdStageDTO));
            devopsCdPipelineDeatilVO.setType(STAGE);
        }
        //阶段审核中，任务处于待审核
        if (AuditStatusEnum.AUDITING.value().equals(devopsCdStageRecordVO.getStatus())) {
            List<DevopsCdJobRecordVO> jobRecordVOList = devopsCdStageRecordVO.getJobRecordVOList();
            if (!CollectionUtils.isEmpty(jobRecordVOList)) {
                jobRecordVOList.sort((o1, o2) -> o1.getSequence().compareTo(o2.getSequence()));
                for (DevopsCdJobRecordVO devopsCdJobRecordVO : jobRecordVOList) {
                    if (AuditStatusEnum.NOT_AUDIT.value().equals(devopsCdJobRecordVO.getStatus())) {
                        devopsCdPipelineDeatilVO = new DevopsCdPipelineDeatilVO();
                        devopsCdPipelineDeatilVO.setStageName(cdStageDTO.getName());
                        devopsCdPipelineDeatilVO.setStageRecordId(devopsCdStageRecordVO.getId());
                        devopsCdPipelineDeatilVO.setTaskRecordId(devopsCdJobRecordVO.getId());
                        devopsCdPipelineDeatilVO.setExecute(executeValue(devopsCdJobRecordVO));
                        devopsCdPipelineDeatilVO.setType(TASK);
                    }
                }
            }
        }
        return devopsCdPipelineDeatilVO;
    }

    private Boolean executeValue(DevopsCdJobRecordVO devopsCdJobRecordVO) {
        List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, null, devopsCdJobRecordVO.getJobId());
        Long userId = DetailsHelper.getUserDetails().getUserId();
        if (!CollectionUtils.isEmpty(devopsCdAuditDTOS)) {
            return devopsCdAuditDTOS.stream().map(DevopsCdAuditDTO::getUserId).
                    collect(Collectors.toList()).contains(userId);
        } else {
            return false;
        }
    }

    private Boolean executeValue(DevopsCdStageDTO cdStageDTO) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditService.baseListByOptions(null, cdStageDTO.getId(), null);
        if (!CollectionUtils.isEmpty(devopsCdAuditDTOS)) {
            return devopsCdAuditDTOS.stream().map(DevopsCdAuditDTO::getUserId).
                    collect(Collectors.toList()).contains(userId);
        } else {
            return false;
        }
    }

    @Override
    public DevopsCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long cdPipelineId) {
        if (Objects.isNull(cdPipelineId) || cdPipelineId == null) {
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
        //查询流水线信息
        CiCdPipelineVO ciCdPipelineVO = devopsCiCdPipelineMapper.queryById(cdPipelineRecordDTO.getPipelineId());
        //添加提交信息
        devopsCdPipelineRecordVO.setCiCdPipelineVO(ciCdPipelineVO);
        addCommitInfo(ciCdPipelineVO.getAppServiceId(), devopsCdPipelineRecordVO, cdPipelineRecordDTO);

        devopsCdPipelineRecordVO.setCiCdPipelineVO(ciCdPipelineVO);
        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(devopsCdPipelineRecordVO.getId());
        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
            List<DevopsCdStageRecordVO> devopsCdStageRecordVOS = ConvertUtils.convertList(devopsCdStageRecordDTOS, this::dtoToVo);
            devopsCdStageRecordVOS.forEach(devopsCdStageRecordVO -> {
                devopsCdStageRecordVO.setType(StageType.CD.getType());
                //查询Cd job
                List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(devopsCdStageRecordVO.getId());
                List<DevopsCdJobRecordVO> devopsCdJobRecordVOS = ConvertUtils.convertList(devopsCdJobRecordDTOS, DevopsCdJobRecordVO.class);
                calculateJob(devopsCdPipelineRecordVO, devopsCdJobRecordVOS);
                devopsCdStageRecordVO.setJobRecordVOList(devopsCdJobRecordVOS);
                //计算stage状态，如果stage里面的阶段任务，全部为未执行，stage状态为未执行
                Set<String> strings = devopsCdJobRecordVOS.stream().map(devopsCdJobRecordVO -> devopsCdJobRecordVO.getStatus()).collect(Collectors.toSet());
                if (!CollectionUtils.isEmpty(strings) && strings.size() == 1 && strings.contains(JobStatusEnum.CREATED.value())) {
                    devopsCdStageRecordVO.setStatus(JobStatusEnum.CREATED.value());
                }
                //计算stage耗时
                List<Long> collect = devopsCdJobRecordVOS.stream().filter(devopsCdJobRecordVO -> !Objects.isNull(devopsCdJobRecordVO.getDurationSeconds())).map(DevopsCdJobRecordVO::getDurationSeconds).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect))
                    devopsCdStageRecordVO.setDurationSeconds(collect.stream().reduce((aLong, aLong2) -> aLong + aLong2).get());
            });
            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(devopsCdStageRecordVOS);
        } else {
            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(Collections.EMPTY_LIST);
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

    private void calculateJob(DevopsCdPipelineRecordVO devopsCdStageRecordVO, List<DevopsCdJobRecordVO> devopsCdJobRecordVOS) {
        devopsCdJobRecordVOS.forEach(devopsCdJobRecordVO -> {
            //计算job耗时
            devopsCdJobRecordVO.setJobExecuteTime();
            //如果是自动部署返回 能点击查看生成实例的相关信息
            if (JobTypeEnum.CD_DEPLOY.value().equals(devopsCdJobRecordVO.getType())) {
                DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = gson.fromJson(devopsCdJobRecordVO.getMetadata(), DevopsCdEnvDeployInfoDTO.class);
                //部署环境 应用服务 生成版本 实例名称
                DevopsCdJobRecordVO.CdAuto cdAuto = devopsCdJobRecordVO.new CdAuto();
                cdAuto.setEnvName(devopsEnvironmentMapper.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getEnvId()).getName());
                cdAuto.setAppServiceName(appServiceMapper.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getAppServiceId()).getName());
                AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
                appServiceVersionDTO.setAppServiceId(devopsCdEnvDeployInfoDTO.getAppServiceId());
                appServiceVersionDTO.setCommit(devopsCdStageRecordVO.getCommitSha());
                List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.select(appServiceVersionDTO);
                if (!CollectionUtils.isEmpty(appServiceVersionDTOS)) {
                    cdAuto.setAppServiceVersion(appServiceVersionDTOS.get(0).getVersion());
                }
                //创建实例
                if (CommandType.CREATE.getType().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
                    //instanceName 根据环境id code来查询
                    AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
                    appServiceInstanceDTO.setCode(devopsCdEnvDeployInfoDTO.getInstanceName());
                    appServiceInstanceDTO.setEnvId(devopsCdEnvDeployInfoDTO.getEnvId());
                    AppServiceInstanceDTO serviceInstanceDTO = appServiceInstanceMapper.selectOne(appServiceInstanceDTO);
                    if (!Objects.isNull(serviceInstanceDTO)) {
                        cdAuto.setInstanceName(serviceInstanceDTO.getCode());
                        cdAuto.setInstanceId(serviceInstanceDTO.getId());
                        cdAuto.setAppServiceId(serviceInstanceDTO.getAppServiceId());
                        cdAuto.setEnvId(serviceInstanceDTO.getEnvId());
                    }
                }
                //替换实例
                if (CommandType.UPDATE.getType().equals(devopsCdEnvDeployInfoDTO.getDeployType())) {
                    AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceMapper.selectByPrimaryKey(devopsCdEnvDeployInfoDTO.getInstanceId());
                    if (appServiceInstanceDTO != null) {
                        cdAuto.setAppServiceName(appServiceInstanceDTO.getCode());
                    }
                }
                devopsCdJobRecordVO.setCdAuto(cdAuto);

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
                            .filter(devopsCdAuditRecordDTO -> AuditStatusEnum.PASSED.value().equals(devopsCdAuditRecordDTO.getStatus()))
                            .map(DevopsCdAuditRecordDTO::getUserId).collect(Collectors.toList()));
                    audit.setReviewedUsers(iamUserDTOS);
                    audit.setStatus(devopsCdJobRecordVO.getStatus());
                }
                devopsCdJobRecordVO.setAudit(audit);
            }
        });

    }

    @Override
    public Boolean testConnection(HostConnectionVO hostConnectionVO) {
        SSHClient ssh = new SSHClient();
        Session session = null;
        Boolean index = true;
        try {
            sshConnect(hostConnectionVO, ssh);
            session = ssh.startSession();
            Session.Command cmd = session.exec("echo Hello World!!!");
            LOGGER.info(IOUtils.readFully(cmd.getInputStream()).toString());
            cmd.join(5, TimeUnit.SECONDS);
            LOGGER.info("\n** exit status: " + cmd.getExitStatus());
        } catch (IOException e) {
            index = false;
            e.printStackTrace();
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

    private Boolean checkInstruction(String type, String instruction) {
        if (type.equals("jar")) {
            return instruction.contains("${jar}");
        } else {
            return instruction.contains("${containerName}") && instruction.contains("${imageName}") && instruction.contains(" -d ");
        }
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
            for (DevopsCdStageRecordVO devopsCdStageRecordVO : devopsCdStageRecordVOS) {
                //查询Cd job
                List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(devopsCdStageRecordVO.getId());
                List<DevopsCdJobRecordVO> devopsCdJobRecordVOS = ConvertUtils.convertList(devopsCdJobRecordDTOS, DevopsCdJobRecordVO.class);
                //计算job耗时
                devopsCdJobRecordVOS.forEach(devopsCdJobRecordVO -> {
                    devopsCdJobRecordVO.setJobExecuteTime();
                });
                //计算cd阶段的状态， cd下的所有job状态都是未执行 那么cd的状态是未执行
                Set<String> strings = devopsCdJobRecordVOS.stream().map(devopsCdJobRecordVO -> devopsCdJobRecordVO.getStatus()).collect(Collectors.toSet());
                if (!CollectionUtils.isEmpty(strings) && strings.size() == 1 && strings.contains(JobStatusEnum.CREATED.value())) {
                    devopsCdStageRecordVO.setStatus(JobStatusEnum.CREATED.value());
                }
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
            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(Collections.EMPTY_LIST);
        }

        return devopsCdPipelineRecordVO;
    }

}
