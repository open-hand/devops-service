package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.hrdsCode.HarborC7nRepoImageTagVo;
import io.choerodon.devops.app.service.DevopsCdAuditRecordService;
import io.choerodon.devops.app.service.DevopsCdJobRecordService;
import io.choerodon.devops.app.service.DevopsCdPipelineRecordService;
import io.choerodon.devops.app.service.DevopsCdStageRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCdAuditRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdPipelineRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusDeployDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineStageDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineTaskDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;
import io.choerodon.devops.infra.mapper.DevopsCdPipelineRecordMapper;
import io.choerodon.devops.infra.mapper.DevopsCdStageRecordMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
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

    public static final Logger LOGGER = LoggerFactory.getLogger(DevopsCdPipelineRecordServiceImpl.class);

    private static final Gson gson = new Gson();

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
            if (!isRetry || i > 0) {
                if (stageRecordDTO.getTriggerType().equals(DeployType.MANUAL.getType())) {
                    List<DevopsCdAuditRecordDTO> stageAuditRecordDTOS = devopsCdAuditRecordService.queryByStageRecordId(stageRecordDTO.getId());
                    if (CollectionUtils.isEmpty(stageAuditRecordDTOS)) {
                        throw new CommonException("error.audit.stage.noUser");
                    }
                    List<String> users = stageAuditRecordDTOS.stream().map(t -> TypeUtil.objToString(t.getUserId())).collect(Collectors.toList());
                    stageDTO.setUsernames(users);
                    stageDTO.setMultiAssign(users.size() > 1);
                }
            }
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
            // 5.
            if (i != stageRecordDTOList.size() - 1) {
                stageDTO.setNextStageTriggerType(stageRecordDTOList.get(i + 1).getTriggerType());
            }
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
        Session session = null;
        try {
            // 0.1
            DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
            CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
            CdHostDeployConfigVO.ImageDeploy imageDeploy = cdHostDeployConfigVO.getImageDeploy();
            // 0.2
            HarborC7nRepoImageTagVo imageTagVo = rdupmClientOperator.listImageTag(imageDeploy.getRepoType(), TypeUtil.objToLong(imageDeploy.getRepoId()), imageDeploy.getImageName());
            List<HarborC7nRepoImageTagVo.HarborC7nImageTagVo> filterImageTagVoList;
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

            // 1. 更新状态 记录镜像信息
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());
            HarborC7nRepoImageTagVo imageTagVoRecord = new HarborC7nRepoImageTagVo();
            BeanUtils.copyProperties(imageTagVo, imageTagVoRecord);
            imageTagVoRecord.setImageTagList(Collections.singletonList(filterImageTagVoList.get(0)));
            jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
            jobRecordDTO.setDeployMetadata(gson.toJson(imageTagVoRecord));
            devopsCdJobRecordService.update(jobRecordDTO);
            // 2.
            session = ssh.startSession();
            sshConnect(cdHostDeployConfigVO.getHostConnectionVO(), ssh);
            // 3.
            // 3.1
            dockerLogin(session, imageTagVo);
            // 3.2
            dockerPull(session, filterImageTagVoList.get(0));
            // 3.3
            dockerRun(session, imageDeploy, filterImageTagVoList.get(0));
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SUCCESS.toValue());
            LOGGER.info("========================================");
            LOGGER.info("image deploy cd host job success!!!,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } catch (Exception e) {
            status = false;
            jobFailed(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } finally {
            closeSsh(ssh, session);
        }
        return status;
    }

    private void dockerLogin(Session session, HarborC7nRepoImageTagVo imageTagVo) throws IOException {
        String loginExec = String.format("docker login -u %s -p %s", imageTagVo.getPullAccount(), imageTagVo.getPullPassword());
        LOGGER.info(loginExec);
        Session.Command cmd = session.exec(loginExec);

        String loggerStr = IOUtils.readFully(cmd.getInputStream()).toString();
        LOGGER.info(loggerStr);
        LOGGER.info("docker login status:{}", cmd.getExitStatus());
        if (loggerStr.contains(UNAUTHORIZED) || !StringUtils.isEmpty(cmd.getExitErrorMessage())) {
            throw new CommonException(ERROR_DOCKER_LOGIN);
        }
    }

    private void dockerPull(Session session, HarborC7nRepoImageTagVo.HarborC7nImageTagVo imageTagVo) throws IOException {
        LOGGER.info(imageTagVo.getPullCmd());
        Session.Command cmd = session.exec(imageTagVo.getPullCmd());
        String loggerStr = IOUtils.readFully(cmd.getInputStream()).toString();
        LOGGER.info(loggerStr);
        LOGGER.info("docker pull status:{}", cmd.getExitStatus());
        if (!StringUtils.isEmpty(cmd.getExitErrorMessage())) {
            throw new CommonException(ERROR_DOCKER_PULL);
        }
    }

    private void dockerRun(Session session, CdHostDeployConfigVO.ImageDeploy imageDeploy, HarborC7nRepoImageTagVo.HarborC7nImageTagVo imageTagVo) throws IOException {
        String dockerRunExec = imageDeploy.getValues().replace("${iamge}", imageTagVo.getPullCmd().replace("docker run ", ""));
        LOGGER.info(dockerRunExec);
        Session.Command cmd = session.exec(dockerRunExec);
        String loggerStr = IOUtils.readFully(cmd.getInputStream()).toString();
        LOGGER.info(loggerStr);
        LOGGER.info("docker run status:{}", cmd.getExitStatus());
        if (!StringUtils.isEmpty(cmd.getExitErrorMessage())) {
            throw new CommonException(ERROR_DOCKER_RUN);
        }
    }

    private void sshConnect(HostConnectionVO hostConnectionVO, SSHClient ssh) throws IOException {
        // 3.
        ssh.loadKnownHosts();
        ssh.connect(hostConnectionVO.getHostIp(), TypeUtil.objToInteger(hostConnectionVO.getHostPort()));
        if (hostConnectionVO.getAccountType().equals(CdHostAccountType.PASSWORD.value())) {
            ssh.authPassword(hostConnectionVO.getUserName(), hostConnectionVO.getPassword());
        } else {
            ssh.authPublickey(hostConnectionVO.getUserName());
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
        Session session = null;
        String jarName = String.format("app-%s.jar", GenerateUUID.generateRandomString());
        try {
            // 0.1
            DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
            CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
            CdHostDeployConfigVO.JarDeploy jarDeploy = cdHostDeployConfigVO.getJarDeploy();
            DevopsCdPipelineRecordDTO cdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(cdPipelineRecordDTO.getProjectId());
            List<C7nNexusComponentDTO> nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), cdPipelineRecordDTO.getProjectId(), jarDeploy.getRepositoryId(), jarDeploy.getGroupId(), jarDeploy.getArtifactId(), jarDeploy.getVersionRegular());
            if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
                devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SKIPPED.toValue());
                LOGGER.info("no jar to deploy,pipelineRecordId:{},cdStageRecordId:{},cdJobRecordId{}", pipelineRecordId, cdStageRecordId, cdJobRecordId);
                return status;
            }
            List<NexusMavenRepoDTO> mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), cdPipelineRecordDTO.getProjectId(), Collections.singleton(jarDeploy.getRepositoryId()));
            if (CollectionUtils.isEmpty(mavenRepoDTOList)) {
                throw new CommonException("error.get.maven.config");
            }

            // 1.
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());
            C7nNexusDeployDTO c7nNexusDeployDTO = new C7nNexusDeployDTO();
            c7nNexusDeployDTO.setC7nNexusComponentDTO(nexusComponentDTOList.get(0));
            c7nNexusDeployDTO.setNexusMavenRepoDTO(mavenRepoDTOList.get(0));
            jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
            jobRecordDTO.setDeployMetadata(gson.toJson(c7nNexusDeployDTO));
            devopsCdJobRecordService.update(jobRecordDTO);
            session = ssh.startSession();

            sshConnect(cdHostDeployConfigVO.getHostConnectionVO(), ssh);
            // 2.1
            sshExec(session, jarName, mavenRepoDTOList.get(0), nexusComponentDTOList.get(0));
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SUCCESS.toValue());
        } catch (Exception e) {
            status = false;
            jobFailed(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } finally {
            try {
                if (session != null) {
                    session.exec(String.format("rm %s", jarName));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            closeSsh(ssh, session);
        }
        return status;
    }

    private void sshExec(Session session, String jarName, NexusMavenRepoDTO mavenRepoDTO, C7nNexusComponentDTO nexusComponentDTO) throws IOException {
        session.exec("mkdir temp-jar");
        session.exec("cd temp-jar");

        // 2.2
        String curlExec = String.format("curl -o %s -u %s:%s %s",
                jarName,
                mavenRepoDTO.getNePullUserId(),
                mavenRepoDTO.getNePullUserPassword(),
                nexusComponentDTO.getDownloadUrl());
        LOGGER.info(curlExec);
        Session.Command cmd = session.exec(curlExec);
        String loggerStr = IOUtils.readFully(cmd.getInputStream()).toString();
        LOGGER.info(loggerStr);
        LOGGER.info("download jar status:{}", cmd.getExitStatus());
        if (!StringUtils.isEmpty(cmd.getExitErrorMessage())) {
            throw new CommonException(ERROR_DOWNLOAD_JAY);
        }

        // 2.3
        String javaJarExec = String.format("java -jar %s", jarName);
        LOGGER.info(javaJarExec);
        cmd = session.exec(curlExec);
        LOGGER.info("exec jar status:{}", cmd.getExitStatus());
        if (!StringUtils.isEmpty(cmd.getExitErrorMessage())) {
            throw new CommonException(ERROR_JAVA_JAR);
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

    private void jobFailed(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.FAILED.toValue());
        devopsCdStageRecordService.updateStatusById(cdStageRecordId, PipelineStatus.FAILED.toValue());
        updateStatusById(pipelineRecordId, PipelineStatus.FAILED.toValue());
    }

    @Override
    public Boolean cdHostDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        DevopsCdJobRecordDTO jobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(jobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.IMAGED_DEPLOY.getValue())) {
            return cdHostImageDeploy(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } else if (cdHostDeployConfigVO.getHostDeployType().equals(HostDeployType.JAR_DEPLOY.getValue())) {
            return cdHostJarDeploy(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } else {
            return true;
        }
    }

    @Override
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
        HarborC7nRepoImageTagVo imageTagVoRecord = gson.fromJson(cdJobRecordDTO.getDeployMetadata(), HarborC7nRepoImageTagVo.class);
        SSHClient ssh = new SSHClient();
        Session session = null;
        try {
            session = ssh.startSession();
            sshConnect(cdHostDeployConfigVO.getHostConnectionVO(), ssh);
            dockerLogin(session, imageTagVoRecord);
            dockerPull(session, imageTagVoRecord.getImageTagList().get(0));
            dockerRun(session, imageDeploy, imageTagVoRecord.getImageTagList().get(0));

            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SUCCESS.toValue());
        } catch (Exception e) {
            jobFailed(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } finally {
            closeSsh(ssh, session);
        }
    }


    private void retryHostJarDeploy(Long pipelineRecordId, Long cdStageRecordId, Long cdJobRecordId) {
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.selectByPrimaryKey(cdJobRecordId);
        devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.RUNNING.toValue());
        CdHostDeployConfigVO cdHostDeployConfigVO = gson.fromJson(cdJobRecordDTO.getMetadata(), CdHostDeployConfigVO.class);
        C7nNexusDeployDTO c7nNexusDeployDTO = gson.fromJson(cdJobRecordDTO.getDeployMetadata(), C7nNexusDeployDTO.class);
        SSHClient ssh = new SSHClient();
        Session session = null;
        String jarName = String.format("app-%s.jar", GenerateUUID.generateRandomString());
        try {
            session = ssh.startSession();
            sshConnect(cdHostDeployConfigVO.getHostConnectionVO(), ssh);
            // 2.1
            sshExec(session, jarName, c7nNexusDeployDTO.getNexusMavenRepoDTO(), c7nNexusDeployDTO.getC7nNexusComponentDTO());
            devopsCdJobRecordService.updateStatusById(cdJobRecordId, PipelineStatus.SUCCESS.toValue());
        } catch (Exception e) {
            jobFailed(pipelineRecordId, cdStageRecordId, cdJobRecordId);
        } finally {
            closeSsh(ssh, session);
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
        devopsCdPipelineRecordDTO.setErrorInfo(errorInfo);
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
                List<DevopsCdStageRecordVO> devopsCdStageRecordVOS = ConvertUtils.convertList(devopsCdStageRecordDTOS, DevopsCdStageRecordVO.class);
                devopsCdStageRecordVOS.forEach(devopsCdStageRecordVO -> {
                    //查询Cd job
                    List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(devopsCdStageRecordVO.getId());
                    List<DevopsCdJobRecordVO> devopsCdJobRecordVOS = ConvertUtils.convertList(devopsCdJobRecordDTOS, DevopsCdJobRecordVO.class);
                    //计算job耗时
                    devopsCdJobRecordVOS.forEach(devopsCdJobRecordVO -> {
                        devopsCdJobRecordVO.setJobExecuteTime();
                    });
                    devopsCdStageRecordVO.setDevopsCdJobRecordVOS(devopsCdJobRecordVOS);
                });
                //计算 stage耗时
//                devopsCdStageRecordVOS.forEach(devopsCdStageRecordVO -> {
//                    devopsCdStageRecordVO.setStageExecuteTime();
//                });
//                devopsCdPipelineRecordVO.setDurationSeconds(devopsCdStageRecordVOS.stream().map(DevopsCdStageRecordVO::getExecutionTime).reduce((aLong, aLong2) -> aLong + aLong2).get());
                devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(devopsCdStageRecordVOS);
            }
        });
        return pipelineRecordInfo;
    }

    @Override
    public DevopsCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId) {
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = new DevopsCdPipelineRecordDTO();
        devopsCdPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
        DevopsCdPipelineRecordDTO cdPipelineRecordDTO = devopsCdPipelineRecordMapper.selectOne(devopsCdPipelineRecordDTO);
        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = ConvertUtils.convertObject(cdPipelineRecordDTO, DevopsCdPipelineRecordVO.class);

        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(devopsCdPipelineRecordVO.getPipelineId());
        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
            List<DevopsCdStageRecordVO> devopsCdStageRecordVOS = ConvertUtils.convertList(devopsCdStageRecordDTOS, DevopsCdStageRecordVO.class);
            devopsCdStageRecordVOS.forEach(devopsCdStageRecordVO -> {
                //查询Cd job
                List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(devopsCdStageRecordVO.getId());
                List<DevopsCdJobRecordVO> devopsCdJobRecordVOS = ConvertUtils.convertList(devopsCdJobRecordDTOS, DevopsCdJobRecordVO.class);
                //计算job耗时
                devopsCdJobRecordVOS.forEach(devopsCdJobRecordVO -> {
                    devopsCdJobRecordVO.setJobExecuteTime();
                });
                devopsCdStageRecordVO.setDevopsCdJobRecordVOS(devopsCdJobRecordVOS);
            });
            //计算 stage耗时
//            devopsCdStageRecordVOS.forEach(devopsCdStageRecordVO -> {
//                devopsCdStageRecordVO.setStageExecuteTime();
//            });
            devopsCdPipelineRecordVO.setDurationSeconds(devopsCdStageRecordVOS.stream().map(DevopsCdStageRecordVO::getExecutionTime).reduce((aLong, aLong2) -> aLong + aLong2).get());
            devopsCdPipelineRecordVO.setDevopsCdStageRecordVOS(devopsCdStageRecordVOS);
        }
        return devopsCdPipelineRecordVO;
    }

    @Override
    public Boolean testConnection(HostConnectionVO hostConnectionVO) {
        SSHClient ssh = new SSHClient();
        Session session = null;
        Boolean index = true;
        try {
            session = ssh.startSession();
            sshConnect(hostConnectionVO, ssh);
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
}
