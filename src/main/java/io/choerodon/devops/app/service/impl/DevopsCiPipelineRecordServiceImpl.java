package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_GITLAB_CI_PIPELINE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.constant.PipelineConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.GitlabPipelineDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.Pipeline;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.handler.CiPipelineSyncHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 9:26
 */
@Service
public class DevopsCiPipelineRecordServiceImpl implements DevopsCiPipelineRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCiPipelineRecordServiceImpl.class);


    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";
    private static final String ERROR_GITLAB_PIPELINE_ID_IS_NULL = "error.gitlab.pipeline.id.is.null";
    private static final String ERROR_GITLAB_PROJECT_ID_IS_NULL = "error.gitlab.project.id.is.null";

    private final DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;
    private final DevopsCiJobRecordService devopsCiJobRecordService;
    private final DevopsCiStageService devopsCiStageService;
    private final DevopsCiJobService devopsCiJobService;
    private final DevopsCiJobRecordMapper devopsCiJobRecordMapper;
    private final DevopsCiPipelineService devopsCiPipelineService;
    private final AppServiceService applicationService;
    private final TransactionalProducer transactionalProducer;
    private final UserAttrService userAttrService;
    private final BaseServiceClientOperator baseServiceClientOperator;
    private final GitlabServiceClientOperator gitlabServiceClientOperator;
    private final DevopsGitlabCommitService devopsGitlabCommitService;
    private final CiPipelineSyncHandler ciPipelineSyncHandler;
    private final CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    private final AppServiceMapper appServiceMapper;
    private DevopsCdPipelineService devopsCdPipelineService;
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;
    private DevopsPipelineRecordRelService devopsPipelineRecordRelService;
    private final DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
    private final AppServiceVersionMapper appServiceVersionMapper;
    private StringRedisTemplate stringRedisTemplate;
    private SendNotificationService sendNotificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // @lazy解决循环依赖
    public DevopsCiPipelineRecordServiceImpl(DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper,
                                             DevopsCiJobRecordService devopsCiJobRecordService,
                                             DevopsCiStageService devopsCiStageService,
                                             @Lazy DevopsCiJobService devopsCiJobService,
                                             DevopsCiJobRecordMapper devopsCiJobRecordMapper,
                                             @Lazy DevopsCiPipelineService devopsCiPipelineService,
                                             AppServiceService applicationService,
                                             TransactionalProducer transactionalProducer,
                                             UserAttrService userAttrService,
                                             AppServiceMapper appServiceMapper,
                                             CheckGitlabAccessLevelService checkGitlabAccessLevelService,
                                             BaseServiceClientOperator baseServiceClientOperator,
                                             GitlabServiceClientOperator gitlabServiceClientOperator,
                                             @Lazy CiPipelineSyncHandler ciPipelineSyncHandler,
                                             DevopsGitlabCommitService devopsGitlabCommitService,
                                             @Lazy DevopsCdPipelineService devopsCdPipelineService,
                                             @Lazy DevopsCdPipelineRecordService devopsCdPipelineRecordService,
                                             @Lazy DevopsPipelineRecordRelService devopsPipelineRecordRelService,
                                             DevopsCiCdPipelineMapper devopsCiCdPipelineMapper,
                                             AppServiceVersionMapper appServiceVersionMapper,
                                             StringRedisTemplate stringRedisTemplate,
                                             SendNotificationService sendNotificationService
    ) {
        this.devopsCiPipelineRecordMapper = devopsCiPipelineRecordMapper;
        this.devopsCiJobRecordService = devopsCiJobRecordService;
        this.devopsCiStageService = devopsCiStageService;
        this.devopsCiJobService = devopsCiJobService;
        this.devopsCiJobRecordMapper = devopsCiJobRecordMapper;
        this.devopsCiPipelineService = devopsCiPipelineService;
        this.applicationService = applicationService;
        this.transactionalProducer = transactionalProducer;
        this.userAttrService = userAttrService;
        this.baseServiceClientOperator = baseServiceClientOperator;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.devopsGitlabCommitService = devopsGitlabCommitService;
        this.ciPipelineSyncHandler = ciPipelineSyncHandler;
        this.checkGitlabAccessLevelService = checkGitlabAccessLevelService;
        this.appServiceMapper = appServiceMapper;
        this.devopsCdPipelineService = devopsCdPipelineService;
        this.devopsCdPipelineRecordService = devopsCdPipelineRecordService;
        this.devopsPipelineRecordRelService = devopsPipelineRecordRelService;
        this.devopsCiCdPipelineMapper = devopsCiCdPipelineMapper;
        this.appServiceVersionMapper = appServiceVersionMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.sendNotificationService = sendNotificationService;
    }

    @Override
    @Saga(code = DEVOPS_GITLAB_CI_PIPELINE, description = "gitlab ci pipeline创建到数据库", inputSchemaClass = PipelineWebHookVO.class)
    public void create(PipelineWebHookVO pipelineWebHookVO, String token) {
        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(token);
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
        if (devopsCiPipelineDTO == null || Boolean.FALSE.equals(devopsCiPipelineDTO.getEnabled())) {
            LOGGER.debug("Skip null of disabled pipeline for pipeline webhook with id {} and token: {}", pipelineWebHookVO.getObjectAttributes().getId(), token);
            return;
        }
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(devopsCiPipelineDTO.getId());
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(devopsCiPipelineDTO.getId());
        Map<Long, DevopsCiStageDTO> stageMap = devopsCiStageDTOList.stream().collect(Collectors.toMap(DevopsCiStageDTO::getId, v -> v));
        Map<String, DevopsCiJobDTO> jobMap = devopsCiJobDTOS.stream().collect(Collectors.toMap(DevopsCiJobDTO::getName, v -> v));
        // 检验是否是手动修改gitlab-ci.yaml文件生成的流水线记录
        for (CiJobWebHookVO job : pipelineWebHookVO.getBuilds()) {
            DevopsCiJobDTO devopsCiJobDTO = jobMap.get(job.getName());
            if (devopsCiJobDTO == null) {
                LOGGER.debug("Job Mismatch {} Skip the pipeline webhook...", job.getName());
                return;
            } else {
                DevopsCiStageDTO devopsCiStageDTO = stageMap.get(devopsCiJobDTO.getCiStageId());
                if (devopsCiStageDTO == null || !devopsCiStageDTO.getName().equals(job.getStage())) {
                    LOGGER.debug("the stage name of the job {} mismatch...", job.getStage());
                    return;
                } else {
                    job.setType(devopsCiJobDTO.getType());
                    job.setMetadata(devopsCiJobDTO.getMetadata());
                }
            }
        }
        pipelineWebHookVO.setToken(token);
        try {
            String input = objectMapper.writeValueAsString(pipelineWebHookVO);
            transactionalProducer.apply(
                    StartSagaBuilder.newBuilder()
                            .withRefType("app")
                            .withRefId(appServiceDTO.getId().toString())
                            .withSagaCode(DEVOPS_GITLAB_CI_PIPELINE)
                            .withLevel(ResourceLevel.PROJECT)
                            .withSourceId(appServiceDTO.getProjectId())
                            .withJson(input),
                    builder -> {
                    });
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public void handleCreate(PipelineWebHookVO pipelineWebHookVO) {
        LOGGER.debug("Start to handle pipeline with gitlab pipeline id {}...", pipelineWebHookVO.getObjectAttributes().getId());
        AppServiceDTO applicationDTO = applicationService.baseQueryByToken(pipelineWebHookVO.getToken());
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(applicationDTO.getId());

        DevopsCiPipelineRecordDTO record = new DevopsCiPipelineRecordDTO();
        record.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(record);
        Long iamUserId = getIamUserIdByGitlabUserName(pipelineWebHookVO.getUser().getUsername());
        CustomContextUtil.setDefaultIfNull(iamUserId);

        //pipeline不存在则创建,存在则更新状态和阶段信息
        if (devopsCiPipelineRecordDTO == null) {
            LOGGER.debug("Start to create pipeline with gitlab pipeline id {}...", pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO = new DevopsCiPipelineRecordDTO();
            devopsCiPipelineRecordDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
            devopsCiPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setTriggerUserId(iamUserId);
            devopsCiPipelineRecordDTO.setCommitSha(pipelineWebHookVO.getObjectAttributes().getSha());
            devopsCiPipelineRecordDTO.setCreatedDate(pipelineWebHookVO.getObjectAttributes().getCreatedAt());
            devopsCiPipelineRecordDTO.setFinishedDate(pipelineWebHookVO.getObjectAttributes().getFinishedAt());
            devopsCiPipelineRecordDTO.setDurationSeconds(pipelineWebHookVO.getObjectAttributes().getDuration());
            devopsCiPipelineRecordDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());
            devopsCiPipelineRecordDTO.setGitlabProjectId(pipelineWebHookVO.getProject().getId());
            devopsCiPipelineRecordDTO.setGitlabTriggerRef(pipelineWebHookVO.getObjectAttributes().getRef());
            devopsCiPipelineRecordMapper.insertSelective(devopsCiPipelineRecordDTO);
            // 保存job执行记录
            Long pipelineRecordId = devopsCiPipelineRecordDTO.getId();
            saveJobRecords(pipelineWebHookVO, pipelineRecordId);

            // 保存流水线记录关系
            DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = new DevopsPipelineRecordRelDTO();
            devopsPipelineRecordRelDTO.setCiPipelineRecordId(pipelineRecordId);
            devopsPipelineRecordRelDTO.setPipelineId(devopsCiPipelineDTO.getId());
            devopsPipelineRecordRelDTO.setCdPipelineRecordId(PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID);
            devopsPipelineRecordRelService.save(devopsPipelineRecordRelDTO);

        } else {
            LOGGER.debug("Start to update pipeline with gitlab pipeline id {}...", pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setTriggerUserId(iamUserId);
            devopsCiPipelineRecordDTO.setCommitSha(pipelineWebHookVO.getObjectAttributes().getSha());
            devopsCiPipelineRecordDTO.setCreatedDate(pipelineWebHookVO.getObjectAttributes().getCreatedAt());
            devopsCiPipelineRecordDTO.setFinishedDate(pipelineWebHookVO.getObjectAttributes().getFinishedAt());
            devopsCiPipelineRecordDTO.setDurationSeconds(pipelineWebHookVO.getObjectAttributes().getDuration());
            devopsCiPipelineRecordDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());
            devopsCiPipelineRecordMapper.updateByPrimaryKeySelective(devopsCiPipelineRecordDTO);
            // 更新job状态
            // 保存job执行记录
            Long pipelineRecordId = devopsCiPipelineRecordDTO.getId();
            saveJobRecords(pipelineWebHookVO, pipelineRecordId);
        }
        if (pipelineWebHookVO.getObjectAttributes().getStatus().equals(JobStatusEnum.FAILED.value())) {
            sendNotificationService.sendCiPipelineNotice(devopsCiPipelineRecordDTO.getId(),
                    MessageCodeConstants.PIPELINE_FAILED, devopsCiPipelineRecordDTO.getCreatedBy(), null, null);
        }
    }

    private void saveJobRecords(PipelineWebHookVO pipelineWebHookVO, Long pipelineRecordId) {
        pipelineWebHookVO.getBuilds().forEach(ciJobWebHookVO -> {
            DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByGitlabJobId(ciJobWebHookVO.getId());
            if (devopsCiJobRecordDTO == null) {
                LOGGER.debug("Start to create job with gitlab job id {}...", ciJobWebHookVO.getId());
                devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
                devopsCiJobRecordDTO.setGitlabJobId(ciJobWebHookVO.getId());
                devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                devopsCiJobRecordDTO.setStage(ciJobWebHookVO.getStage());
                devopsCiJobRecordDTO.setType(ciJobWebHookVO.getType());
                devopsCiJobRecordDTO.setName(ciJobWebHookVO.getName());
                devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus());
                devopsCiJobRecordDTO.setTriggerUserId(getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                devopsCiJobRecordDTO.setGitlabProjectId(pipelineWebHookVO.getProject().getId());
                devopsCiJobRecordDTO.setMetadata(ciJobWebHookVO.getMetadata());
                devopsCiJobRecordMapper.insertSelective(devopsCiJobRecordDTO);
            } else {
                LOGGER.debug("Start to update job with gitlab job id {}...", ciJobWebHookVO.getId());
                devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus());
                devopsCiJobRecordDTO.setTriggerUserId(getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiJobRecordMapper, devopsCiJobRecordDTO, "error.update.ci.job.record", ciJobWebHookVO.getId());
                // sonar任务执行成功后，缓存sonar信息到redis
                if (PipelineStatus.SUCCESS.toValue().equals(ciJobWebHookVO.getStatus())
                        && JobTypeEnum.SONAR.value().equals(devopsCiJobRecordDTO.getType())) {
                    DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
                    CiCdPipelineVO ciCdPipelineVO = devopsCiPipelineService.queryById(devopsCiPipelineRecordDTO.getCiPipelineId());
                    applicationService.getSonarContent(ciCdPipelineVO.getProjectId(), ciCdPipelineVO.getAppServiceId());
                }
            }
        });
    }

    @Override
    public Page<DevopsCiPipelineRecordVO> pagingPipelineRecord(Long projectId, Long ciPipelineId, PageRequest pageable) {
        Page<DevopsCiPipelineRecordVO> pipelineRecordInfo = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> devopsCiPipelineRecordMapper.listByCiPipelineId(ciPipelineId));
        List<DevopsCiPipelineRecordVO> pipelineRecordVOList = pipelineRecordInfo.getContent();
        if (CollectionUtils.isEmpty(pipelineRecordVOList)) {
            return pipelineRecordInfo;
        }
        pipelineRecordVOList.forEach(pipelineRecord -> {
            ciPipelineSyncHandler.syncPipeline(pipelineRecord.getStatus(), pipelineRecord.getLastUpdateDate(), pipelineRecord.getId(), TypeUtil.objToInteger(pipelineRecord.getGitlabPipelineId()));
            // 查询流水线记录下的job记录
            DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
            recordDTO.setCiPipelineRecordId(pipelineRecord.getId());
            List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS = devopsCiJobRecordMapper.select(recordDTO);

            // 只返回job的最新记录
            devopsCiJobRecordDTOS = filterJobs(devopsCiJobRecordDTOS);
            Map<String, List<DevopsCiJobRecordDTO>> jobRecordMap = devopsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStage));
            // 查询阶段信息
            List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(ciPipelineId);
            List<DevopsCiStageRecordVO> devopsCiStageRecordVOS = ConvertUtils.convertList(devopsCiStageDTOList, DevopsCiStageRecordVO.class);
            // 计算stage状态
            devopsCiStageRecordVOS.forEach(stageRecord -> {
                List<DevopsCiJobRecordDTO> ciJobRecordDTOS = jobRecordMap.get(stageRecord.getName());
                if (!CollectionUtils.isEmpty(ciJobRecordDTOS)) {
                    Map<String, List<DevopsCiJobRecordDTO>> statusMap = ciJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStatus));
                    //计算stage状态
                    calculateStageStatus(stageRecord, statusMap);
                }

            });
            // stage排序
            devopsCiStageRecordVOS = devopsCiStageRecordVOS.stream().sorted(Comparator.comparing(DevopsCiStageRecordVO::getSequence)).filter(v -> v.getStatus() != null).collect(Collectors.toList());
            pipelineRecord.setStageRecordVOList(devopsCiStageRecordVOS);
        });
        return pipelineRecordInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Async(GitOpsConstants.PIPELINE_EXECUTOR)
    @Override
    public void asyncPipelineUpdate(Long pipelineRecordId, Integer gitlabPipelineId) {
        LOGGER.info("Start to update pipeline asynchronously...record id {}, gitlab pipeline id {}", pipelineRecordId, gitlabPipelineId);
        AssertUtils.notNull(pipelineRecordId, "pipelineRecordId shouldn't be null");

        AppServiceDTO appServiceDTO = devopsCiPipelineRecordMapper.queryGitlabProjectIdByRecordId(pipelineRecordId);
        Integer gitlabProjectId = appServiceDTO.getGitlabProjectId();
        GitlabPipelineDTO pipelineDTO = gitlabServiceClientOperator.queryPipeline(TypeUtil.objToInteger(gitlabProjectId),
                TypeUtil.objToInteger(gitlabPipelineId),
                GitUserNameUtil.getAdminId());
        List<JobDTO> jobDTOList = gitlabServiceClientOperator.listJobs(gitlabProjectId, gitlabPipelineId, GitUserNameUtil.getAdminId());

        Long gitlabPipelineIdLong = TypeUtil.objToLong(gitlabPipelineId);
        handUpdate(appServiceDTO, pipelineRecordId, gitlabPipelineIdLong, pipelineDTO, jobDTOList);
    }

    private void handUpdate(AppServiceDTO appServiceDTO, Long pipelineRecordId, Long gitlabPipelineId, GitlabPipelineDTO gitlabPipelineDTO, List<JobDTO> jobs) {
        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());

        DevopsCiPipelineRecordDTO record = new DevopsCiPipelineRecordDTO();
        record.setGitlabPipelineId(gitlabPipelineId);
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
        CustomContextUtil.setDefault();

        LOGGER.debug("Start to update pipeline with gitlab pipeline id {}...", gitlabPipelineId);
        devopsCiPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsCiPipelineRecordDTO.setDurationSeconds(TypeUtil.objToLong(gitlabPipelineDTO.getDuration()));
        devopsCiPipelineRecordDTO.setStatus(gitlabPipelineDTO.getStatus().toValue());
        devopsCiPipelineRecordMapper.updateByPrimaryKeySelective(devopsCiPipelineRecordDTO);
        // 如果流水线状态更新为成功，则执行cd流水线触发逻辑
        if (PipelineStatus.SUCCESS.toValue().equals(gitlabPipelineDTO.getStatus().toValue())) {
            DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryByGitlabPipelineId(devopsCiPipelineRecordDTO.getGitlabPipelineId());
            if (devopsCdPipelineRecordDTO != null) {
                devopsCdPipelineService.executeCdPipeline(devopsCdPipelineRecordDTO.getId());
            }
        }

        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(devopsCiPipelineDTO.getId());
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(devopsCiPipelineDTO.getId());
        Map<Long, DevopsCiStageDTO> stageMap = devopsCiStageDTOList.stream().collect(Collectors.toMap(DevopsCiStageDTO::getId, v -> v));
        Map<String, DevopsCiJobDTO> jobMap = devopsCiJobDTOS.stream().collect(Collectors.toMap(DevopsCiJobDTO::getName, v -> v));

        // 检验是否是手动修改gitlab-ci.yaml文件生成的流水线记录
        // 如果不符合流水线设置， 提前退出， 只同步流水线的状态， stage的跳过
        Map<Integer, String> jobType = new HashMap<>();
        for (JobDTO job : jobs) {
            DevopsCiJobDTO devopsCiJobDTO = jobMap.get(job.getName());
            if (devopsCiJobDTO == null) {
                LOGGER.debug("Job Mismatch {} Skip the pipeline webhook...", job.getName());
                return;
            } else {
                DevopsCiStageDTO devopsCiStageDTO = stageMap.get(devopsCiJobDTO.getCiStageId());
                if (devopsCiStageDTO == null || !devopsCiStageDTO.getName().equals(job.getStage())) {
                    LOGGER.debug("the stage name of the job {} mismatch...", job.getStage());
                    return;
                } else {
                    jobType.put(job.getId(), devopsCiJobDTO.getType());
                }
            }
        }

        // 更新job状态
        // 保存job执行记录
        saveJobRecords(TypeUtil.objToLong(appServiceDTO.getGitlabProjectId()), pipelineRecordId, jobs, jobType);
    }

    private void saveJobRecords(Long gitlabProjectId, Long pipelineRecordId, List<JobDTO> jobs, Map<Integer, String> jobType) {
        jobs.forEach(ciJobWebHookVO -> {
            Long jobId = TypeUtil.objToLong(ciJobWebHookVO.getId());
            DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByGitlabJobId(jobId);
            if (devopsCiJobRecordDTO == null) {
                LOGGER.debug("Start to create job with gitlab job id {}...", ciJobWebHookVO.getId());
                devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
                devopsCiJobRecordDTO.setGitlabJobId(jobId);
                devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                devopsCiJobRecordDTO.setStage(ciJobWebHookVO.getStage());
                devopsCiJobRecordDTO.setType(jobType.get(ciJobWebHookVO.getId()));
                devopsCiJobRecordDTO.setName(ciJobWebHookVO.getName());
                devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus().toValue());
                devopsCiJobRecordDTO.setGitlabProjectId(gitlabProjectId);
                devopsCiJobRecordDTO.setTriggerUserId(getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                devopsCiJobRecordMapper.insertSelective(devopsCiJobRecordDTO);
            } else {
                LOGGER.debug("Start to update job with gitlab job id {}...", ciJobWebHookVO.getId());
                devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus().toValue());
                devopsCiJobRecordDTO.setTriggerUserId(getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiJobRecordMapper, devopsCiJobRecordDTO, "error.update.ci.job.record", ciJobWebHookVO.getId());
            }
        });
    }

    private List<DevopsCiJobRecordDTO> filterJobs(List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS) {
        List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(devopsCiJobRecordDTOS)) {
            return devopsCiJobRecordDTOList;
        }
        Map<String, List<DevopsCiJobRecordDTO>> jobMap = devopsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getName));
        jobMap.forEach((k, v) -> {
            if (v.size() > 1) {
                Optional<DevopsCiJobRecordDTO> ciJobRecordDTO = v.stream().max(Comparator.comparing(DevopsCiJobRecordDTO::getId));
                devopsCiJobRecordDTOList.add(ciJobRecordDTO.get());
            } else if (v.size() == 1) {
                devopsCiJobRecordDTOList.add(v.get(0));
            }
        });
        return devopsCiJobRecordDTOList;
    }

    @Override
    public DevopsCiPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long ciPipelineRecordId) {
        if (ciPipelineRecordId == null || ciPipelineRecordId == 0L) {
            return null;
        }
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(ciPipelineRecordId);

        ciPipelineSyncHandler.syncPipeline(devopsCiPipelineRecordDTO.getStatus(), devopsCiPipelineRecordDTO.getLastUpdateDate(), devopsCiPipelineRecordDTO.getId(), TypeUtil.objToInteger(devopsCiPipelineRecordDTO.getGitlabPipelineId()));

        DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = ConvertUtils.convertObject(devopsCiPipelineRecordDTO, DevopsCiPipelineRecordVO.class);
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(devopsCiPipelineRecordDTO.getTriggerUserId());
        devopsCiPipelineRecordVO.setUserDTO(iamUserDTO);
        devopsCiPipelineRecordVO.setUsername(iamUserDTO.getRealName());
        devopsCiPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordDTO.getCreationDate());

        // 添加提交信息
        CiCdPipelineVO ciCdPipelineVO = devopsCiPipelineService.queryById(devopsCiPipelineRecordDTO.getCiPipelineId());
        devopsCiPipelineRecordVO.setDevopsCiPipelineVO(ciCdPipelineVO);
        addCommitInfo(ciCdPipelineVO.getAppServiceId(), devopsCiPipelineRecordVO, devopsCiPipelineRecordDTO);

        // 查询流水线记录下的job记录
        DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
        recordDTO.setCiPipelineRecordId(devopsCiPipelineRecordDTO.getId());
        List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS = devopsCiJobRecordMapper.select(recordDTO);

        Map<String, List<DevopsCiJobRecordDTO>> jobRecordMap = devopsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStage));

        List<DevopsCiStageRecordVO> devopsCiStageRecordVOS = new ArrayList<>();
        for (Map.Entry<String, List<DevopsCiJobRecordDTO>> entry : jobRecordMap.entrySet()) {
            String k = entry.getKey();
            List<DevopsCiJobRecordDTO> value = entry.getValue();
            DevopsCiStageRecordVO devopsCiStageRecordVO = new DevopsCiStageRecordVO();
            devopsCiStageRecordVO.setName(k);
            devopsCiStageRecordVO.setType(StageType.CI.getType());
            devopsCiStageRecordVO.setSequence(value.stream().min(Comparator.comparing(DevopsCiJobRecordDTO::getGitlabJobId)).get().getGitlabJobId());
            // 只返回job的最新记录
            List<DevopsCiJobRecordDTO> latestedsCiJobRecordDTOS = filterJobs(value);
            Map<String, List<DevopsCiJobRecordDTO>> statusMap = latestedsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStatus));
            calculateStageStatus(devopsCiStageRecordVO, statusMap);
            List<DevopsCiJobRecordVO> latestedsCiJobRecordVOS = ConvertUtils.convertList(latestedsCiJobRecordDTOS, DevopsCiJobRecordVO.class);
            latestedsCiJobRecordVOS.forEach(devopsCiJobRecordVO -> {
                if (JobTypeEnum.SONAR.value().equals(devopsCiJobRecordVO.getType())) {
                    if (StringUtils.isNotBlank(devopsCiJobRecordVO.getMetadata())) {
                        SonarQubeConfigVO sonarQubeConfigVO = JSONObject.parseObject(devopsCiJobRecordVO.getMetadata(), SonarQubeConfigVO.class);
                        devopsCiJobRecordVO.setSonarScannerType(sonarQubeConfigVO.getScannerType());
                    }
                    // 执行成功的添加sonar信息
                    if (PipelineStatus.SUCCESS.toValue().equals(devopsCiJobRecordVO.getStatus())) {
                        SonarContentsVO sonarContentsVO = applicationService.getSonarContentFromCache(ciCdPipelineVO.getProjectId(), ciCdPipelineVO.getAppServiceId());
                        if (!Objects.isNull(sonarContentsVO) && !CollectionUtils.isEmpty(sonarContentsVO.getSonarContents())) {
                            List<SonarContentVO> sonarContents = sonarContentsVO.getSonarContents();
                            List<SonarContentVO> sonarContentVOS = sonarContents.stream().filter(sonarContentVO -> {
                                return SonarQubeType.BUGS.getType().equals(sonarContentVO.getKey())
                                        || SonarQubeType.CODE_SMELLS.getType().equals(sonarContentVO.getKey())
                                        || SonarQubeType.VULNERABILITIES.getType().equals(sonarContentVO.getKey());
                            }).collect(Collectors.toList());

                            sonarContents.forEach(v -> {
                                if (SonarQubeType.COVERAGE.getType().equals(v.getKey())) {
                                    devopsCiJobRecordVO.setCodeCoverage(v.getValue());
                                }
                            });
                            devopsCiJobRecordVO.setSonarContentVOS(sonarContentVOS);
                        }
                    }

                }
                //release阶段，添加版本的信息
                if (JobTypeEnum.CHART.value().equals(devopsCiJobRecordVO.getType())) {
                    CiCdPipelineDTO ciCdPipelineDTO = devopsCiCdPipelineMapper.selectByPrimaryKey(devopsCiPipelineRecordDTO.getCiPipelineId());
                    if (!Objects.isNull(ciCdPipelineDTO)) {
                        String commitSha = devopsCiPipelineRecordVO.getCommit().getCommitSha();
                        String ref = devopsCiPipelineRecordVO.getCommit().getRef();
                        AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();
                        appServiceVersionDTO.setCommit(commitSha);
                        appServiceVersionDTO.setRef(ref);
                        appServiceVersionDTO.setAppServiceId(ciCdPipelineDTO.getAppServiceId());
                        List<AppServiceVersionDTO> appServiceVersionDTOS = appServiceVersionMapper.select(appServiceVersionDTO);
                        if (!CollectionUtils.isEmpty(appServiceVersionDTOS)) {
                            devopsCiJobRecordVO.setChartVersion(appServiceVersionDTOS.get(0).getVersion());
                        }
                    }
                }
            });
            devopsCiStageRecordVO.setDurationSeconds(calculateStageDuration(latestedsCiJobRecordVOS));
            // 按照gitlab job id正序排序
            latestedsCiJobRecordVOS.sort(Comparator.comparingLong(DevopsCiJobRecordVO::getGitlabJobId));
            devopsCiStageRecordVO.setJobRecordVOList(latestedsCiJobRecordVOS);

            devopsCiStageRecordVOS.add(devopsCiStageRecordVO);
        }


//        Map<String, List<DevopsCiJobRecordVO>> jobRecordMap = devopsCiJobRecordVOList.stream().collect(Collectors.groupingBy(DevopsCiJobRecordVO::getStage));

        // 查询阶段信息
//        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(devopsCiPipelineRecordDTO.getCiPipelineId());
//        if (CollectionUtils.isEmpty(devopsCiStageDTOList)) {
//            for (Map.Entry<String, List<DevopsCiJobRecordVO>> stringListEntry : jobRecordMap.entrySet()) {
//                long seq = 0L;
//                DevopsCiStageDTO devopsCiStageDTO = new DevopsCiStageDTO();
//                devopsCiStageDTO.setName(stringListEntry.getKey());
//                devopsCiStageDTO.setSequence(seq++);
//                devopsCiStageDTOList.add(devopsCiStageDTO);
//            }
//        }
//        List<DevopsCiStageRecordVO> devopsCiStageRecordVOS = ConvertUtils.convertList(devopsCiStageDTOList, DevopsCiStageRecordVO.class);

//        devopsCiStageRecordVOS.forEach(devopsCiStageRecordVO -> {
//            devopsCiStageRecordVO.setType(StageType.CI.getType());
//        });
        // 计算stage状态
//        devopsCiStageRecordVOS.forEach(stageRecord -> {
//            List<DevopsCiJobRecordVO> devopsCiJobRecordVOS = jobRecordMap.get(stageRecord.getName());
//            if (!CollectionUtils.isEmpty(devopsCiJobRecordVOS)) {
//                Map<String, List<DevopsCiJobRecordVO>> statusMap = devopsCiJobRecordVOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordVO::getStatus));
//                calculateStageStatus(stageRecord, statusMap);
//                // 计算stage耗时
//                stageRecord.setDurationSeconds(calculateStageDuration(devopsCiJobRecordVOS));
//                // 按照gitlab job id正序排序
//                devopsCiJobRecordVOS.sort(Comparator.comparingLong(DevopsCiJobRecordVO::getGitlabJobId));
//                stageRecord.setJobRecordVOList(devopsCiJobRecordVOS);
//            }
//        });
        // stage排序
        devopsCiStageRecordVOS = devopsCiStageRecordVOS.stream().sorted(Comparator.comparing(DevopsCiStageRecordVO::getSequence)).filter(v -> v.getStatus() != null).collect(Collectors.toList());
        devopsCiPipelineRecordVO.setStageRecordVOList(devopsCiStageRecordVOS);

        return devopsCiPipelineRecordVO;
    }

    /**
     * 添加提交信息
     */
    private void addCommitInfo(Long appServiceId, DevopsCiPipelineRecordVO devopsCiPipelineRecordVO, DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO) {
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(devopsCiPipelineRecordDTO.getCommitSha(), devopsCiPipelineRecordDTO.getGitlabTriggerRef());

        CustomCommitVO customCommitVO = new CustomCommitVO();
        devopsCiPipelineRecordVO.setCommit(customCommitVO);

        customCommitVO.setGitlabProjectUrl(applicationService.calculateGitlabProjectUrlWithSuffix(appServiceId));

        // 可能因为GitLab webhook 失败, commit信息查不出
        if (devopsGitlabCommitDTO == null) {
            return;
        }
        IamUserDTO commitUser = null;
        if (devopsGitlabCommitDTO.getUserId() != null) {
            commitUser = baseServiceClientOperator.queryUserByUserId(devopsGitlabCommitDTO.getUserId());
        }

        customCommitVO.setRef(devopsCiPipelineRecordDTO.getGitlabTriggerRef());
        customCommitVO.setCommitSha(devopsCiPipelineRecordDTO.getCommitSha());
        customCommitVO.setCommitContent(devopsGitlabCommitDTO.getCommitContent());
        customCommitVO.setCommitUrl(devopsGitlabCommitDTO.getUrl());

        if (commitUser != null) {
            customCommitVO.setUserHeadUrl(commitUser.getImageUrl());
            customCommitVO.setUserName(commitUser.getLdap() ? commitUser.getLoginName() : commitUser.getEmail());
        }
    }

    private Long calculateStageDuration(List<DevopsCiJobRecordVO> devopsCiJobRecordVOS) {
        Optional<DevopsCiJobRecordVO> max = devopsCiJobRecordVOS.stream().filter(v -> v.getDurationSeconds() != null).max(Comparator.comparingInt(v -> v.getDurationSeconds().intValue()));
        return max.orElse(new DevopsCiJobRecordVO()).getDurationSeconds();
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        DevopsCiPipelineRecordDTO pipelineRecordDTO = new DevopsCiPipelineRecordDTO();
        pipelineRecordDTO.setCiPipelineId(ciPipelineId);
        devopsCiPipelineRecordMapper.delete(pipelineRecordDTO);
    }

    @Override
    public List<DevopsCiPipelineRecordDTO> queryByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        DevopsCiPipelineRecordDTO pipelineRecordDTO = new DevopsCiPipelineRecordDTO();
        pipelineRecordDTO.setCiPipelineId(ciPipelineId);
        return devopsCiPipelineRecordMapper.select(pipelineRecordDTO);
    }

    @Override
    @Transactional
    public void deleteByGitlabProjectId(Long gitlabProjectId) {
        Objects.requireNonNull(gitlabProjectId);
        DevopsCiPipelineRecordDTO pipelineRecordDTO = new DevopsCiPipelineRecordDTO();
        pipelineRecordDTO.setGitlabProjectId(gitlabProjectId);
        devopsCiPipelineRecordMapper.delete(pipelineRecordDTO);
    }

    @Override
    public DevopsCiPipelineRecordDTO create(Long ciPipelineId, Long gitlabProjectId, Pipeline pipeline) {
        DevopsCiPipelineRecordDTO pipelineRecordDTO = new DevopsCiPipelineRecordDTO();
        pipelineRecordDTO.setCiPipelineId(ciPipelineId);
        pipelineRecordDTO.setGitlabProjectId(gitlabProjectId);
        pipelineRecordDTO.setGitlabPipelineId(TypeUtil.objToLong(pipeline.getId()));
        pipelineRecordDTO.setCreatedDate(pipeline.getCreatedAt());
        pipelineRecordDTO.setFinishedDate(pipeline.getFinished_at());
        pipelineRecordDTO.setDurationSeconds(TypeUtil.objToLong(pipeline.getDuration()));
        pipelineRecordDTO.setStatus(pipeline.getStatus().toValue());
        pipelineRecordDTO.setTriggerUserId(DetailsHelper.getUserDetails().getUserId());
        pipelineRecordDTO.setGitlabTriggerRef(pipeline.getRef());
        pipelineRecordDTO.setCommitSha(pipeline.getSha());
        devopsCiPipelineRecordMapper.insertSelective(pipelineRecordDTO);
        return devopsCiPipelineRecordMapper.selectByPrimaryKey(pipelineRecordDTO.getId());
    }

    @Override
    public void retry(Long projectId, Long gitlabPipelineId, Long gitlabProjectId) {
        Assert.notNull(gitlabPipelineId, ERROR_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(gitlabProjectId, ERROR_GITLAB_PROJECT_ID_IS_NULL);
        AppServiceDTO appServiceDTO = appServiceMapper.selectOne(new AppServiceDTO().setGitlabProjectId(TypeUtil.objToInteger(gitlabProjectId)));
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceDTO.getId(), AppServiceEvent.CI_PIPELINE_RETRY);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        checkUserBranchPushPermission(projectId, gitlabPipelineId, gitlabProjectId, userAttrDTO.getGitlabUserId());
        // 重试pipeline
        Pipeline pipeline = gitlabServiceClientOperator.retryPipeline(gitlabProjectId.intValue(), gitlabPipelineId.intValue(), userAttrDTO.getGitlabUserId().intValue());

        try {
            // 更新pipeline status
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = updatePipelineStatus(gitlabPipelineId, pipeline.getStatus().toValue());
            // 更新job status
            List<JobDTO> jobDTOS = gitlabServiceClientOperator.listJobs(gitlabProjectId.intValue(), gitlabPipelineId.intValue(), userAttrDTO.getGitlabUserId().intValue());
            updateOrInsertJobRecord(devopsCiPipelineRecordDTO.getId(), gitlabProjectId, jobDTOS, userAttrDTO.getIamUserId());
        } catch (Exception e) {
            LOGGER.info("update pipeline Records failed， gitlabPipelineId {}.", gitlabPipelineId);
        }

    }

    @Override
    public void cancel(Long projectId, Long gitlabPipelineId, Long gitlabProjectId) {
        Assert.notNull(gitlabPipelineId, ERROR_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(gitlabProjectId, ERROR_GITLAB_PROJECT_ID_IS_NULL);
        AppServiceDTO appServiceDTO = appServiceMapper.selectOne(new AppServiceDTO().setGitlabProjectId(TypeUtil.objToInteger(gitlabProjectId)));
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceDTO.getId(), AppServiceEvent.CI_PIPELINE_CANCEL);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(DetailsHelper.getUserDetails().getUserId());
        checkUserBranchPushPermission(projectId, gitlabPipelineId, gitlabProjectId, userAttrDTO.getGitlabUserId());

        gitlabServiceClientOperator.cancelPipeline(gitlabProjectId.intValue(), gitlabPipelineId.intValue(), userAttrDTO.getGitlabUserId().intValue());

        try {
            // 更新pipeline status
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = updatePipelineStatus(gitlabPipelineId, PipelineStatus.CANCELED.toValue());
            // 更新job status
            List<JobDTO> jobDTOS = gitlabServiceClientOperator.listJobs(gitlabProjectId.intValue(), gitlabPipelineId.intValue(), userAttrDTO.getGitlabUserId().intValue());
            updateOrInsertJobRecord(devopsCiPipelineRecordDTO.getId(), gitlabProjectId, jobDTOS, userAttrDTO.getIamUserId());
        } catch (Exception e) {
            LOGGER.info("update pipeline Records failed， gitlabPipelineId {}.", gitlabPipelineId);
        }
    }

    @Override
    public DevopsCiPipelineRecordDTO queryById(Long ciPipelineRecordId) {
        return devopsCiPipelineRecordMapper.selectByPrimaryKey(ciPipelineRecordId);
    }

    @Override
    public DevopsCiPipelineRecordDTO queryByGitlabPipelineId(Long gitlabPipelineId) {
        Assert.notNull(gitlabPipelineId, ERROR_GITLAB_PIPELINE_ID_IS_NULL);
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = new DevopsCiPipelineRecordDTO();
        devopsCiPipelineRecordDTO.setGitlabPipelineId(gitlabPipelineId);
        return devopsCiPipelineRecordMapper.selectOne(devopsCiPipelineRecordDTO);
    }

    @Override
    public List<DevopsCiPipelineRecordDTO> queryNotSynchronizedRecord(Long statusUpdatePeriodMilliSeconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateStr = sdf.format(new Date());
        Date currentDate = null;
        try {
            currentDate = sdf.parse(currentDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return devopsCiPipelineRecordMapper.queryNotSynchronizedRecord(new Date(System.currentTimeMillis() - statusUpdatePeriodMilliSeconds), currentDate);
    }

    /**
     * 校验用户是否有分支权限
     */
    private void checkUserBranchPushPermission(Long projectId, Long gitlabPipelineId, Long gitlabProjectId, Long gitlabUserId) {
        DevopsCiPipelineRecordDTO recordDTO = new DevopsCiPipelineRecordDTO();
        recordDTO.setGitlabPipelineId(gitlabPipelineId);
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(recordDTO);
        devopsCiPipelineService.checkUserBranchPushPermission(projectId, gitlabUserId, gitlabProjectId, devopsCiPipelineRecordDTO.getGitlabTriggerRef());
    }

    /**
     * 更新pipeline status
     */
    private DevopsCiPipelineRecordDTO updatePipelineStatus(Long gitlabPipelineId, String status) {
        DevopsCiPipelineRecordDTO recordDTO = new DevopsCiPipelineRecordDTO();
        recordDTO.setGitlabPipelineId(gitlabPipelineId);
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(recordDTO);
        devopsCiPipelineRecordDTO.setStatus(status);
        devopsCiPipelineRecordMapper.updateByPrimaryKeySelective(devopsCiPipelineRecordDTO);
        return devopsCiPipelineRecordDTO;
    }

    private void updateOrInsertJobRecord(Long ciPipelineRecordId, Long gitlabProjectId, List<JobDTO> jobDTOS, Long iamUserId) {
        jobDTOS.forEach(jobDTO -> {
            DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
            recordDTO.setGitlabJobId(TypeUtil.objToLong(jobDTO.getId()));
            DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordMapper.selectOne(recordDTO);
            // job记录存在则更新，不存在则插入
            if (devopsCiJobRecordDTO == null) {
                devopsCiJobRecordService.create(ciPipelineRecordId, gitlabProjectId, jobDTO, iamUserId);
            } else {
                devopsCiJobRecordDTO.setGitlabJobId(jobDTO.getId().longValue());
                devopsCiJobRecordDTO.setTriggerUserId(iamUserId);
                devopsCiJobRecordDTO.setStatus(jobDTO.getStatus().toValue());
                devopsCiJobRecordMapper.updateByPrimaryKeySelective(devopsCiJobRecordDTO);
            }
        });

    }


    private <T> void calculateStageStatus(DevopsCiStageRecordVO stageRecord, Map<String, List<T>> statusMap) {
        if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.CREATED.value()))) {
            stageRecord.setStatus(JobStatusEnum.CREATED.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.PENDING.value()))) {
            stageRecord.setStatus(JobStatusEnum.PENDING.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.RUNNING.value()))) {
            stageRecord.setStatus(JobStatusEnum.RUNNING.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.FAILED.value()))) {
            stageRecord.setStatus(JobStatusEnum.FAILED.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.SUCCESS.value()))) {
            stageRecord.setStatus(JobStatusEnum.SUCCESS.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.CANCELED.value()))) {
            stageRecord.setStatus(JobStatusEnum.CANCELED.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.SKIPPED.value()))) {
            stageRecord.setStatus(JobStatusEnum.SKIPPED.value());
        } else if (!CollectionUtils.isEmpty(statusMap.get(JobStatusEnum.MANUAL.value()))) {
            stageRecord.setStatus(JobStatusEnum.MANUAL.value());
        }
    }

    private Long getIamUserIdByGitlabUserName(String username) {
        if ("admin1".equals(username) || "root".equals(username)) {
            return 1L;
        }
        UserAttrDTO userAttrE = userAttrService.baseQueryByGitlabUserName(username);
        return userAttrE != null ? userAttrE.getIamUserId() : 0L;
    }

    @Override
    public DevopsCiPipelineRecordVO queryByCiPipelineRecordId(Long ciPipelineRecordId) {
        if (ciPipelineRecordId == null || ciPipelineRecordId == 0L) {
            return null;
        }
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(ciPipelineRecordId);
        if (Objects.isNull(devopsCiPipelineRecordDTO)) {
            return null;
        }
        DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = new DevopsCiPipelineRecordVO();
        BeanUtils.copyProperties(devopsCiPipelineRecordDTO, devopsCiPipelineRecordVO);
        devopsCiPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordDTO.getCreatedDate());

        ciPipelineSyncHandler.syncPipeline(devopsCiPipelineRecordVO.getStatus(), devopsCiPipelineRecordVO.getLastUpdateDate(), devopsCiPipelineRecordVO.getId(), TypeUtil.objToInteger(devopsCiPipelineRecordVO.getGitlabPipelineId()));
        // 查询流水线记录下的job记录
        DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
        recordDTO.setCiPipelineRecordId(devopsCiPipelineRecordVO.getId());
        List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS = devopsCiJobRecordMapper.select(recordDTO);


        Map<String, List<DevopsCiJobRecordDTO>> jobRecordMap = devopsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStage));
        // 查询阶段信息
//        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(devopsCiPipelineRecordDTO.getCiPipelineId());
//        if (CollectionUtils.isEmpty(devopsCiStageDTOList)) {
//            for (Map.Entry<String, List<DevopsCiJobRecordDTO>> stringListEntry : jobRecordMap.entrySet()) {
//                long seq = 0L;
//                DevopsCiStageDTO devopsCiStageDTO = new DevopsCiStageDTO();
//                devopsCiStageDTO.setName(stringListEntry.getKey());
//                devopsCiStageDTO.setSequence(seq++);
//                devopsCiStageDTOList.add(devopsCiStageDTO);
//            }
//        }
//        List<DevopsCiStageRecordVO> devopsCiStageRecordVOS = ConvertUtils.convertList(devopsCiStageDTOList, DevopsCiStageRecordVO.class);
//        // 计算stage状态
//        devopsCiStageRecordVOS.forEach(stageRecord -> {
//            List<DevopsCiJobRecordDTO> ciJobRecordDTOS = jobRecordMap.get(stageRecord.getName());
//            if (!CollectionUtils.isEmpty(ciJobRecordDTOS)) {
//                //计算stage状态
//                calculateStageStatus(stageRecord, statusMap);
//            }
//
//        });


        List<DevopsCiStageRecordVO> devopsCiStageRecordVOS = new ArrayList<>();
        for (Map.Entry<String, List<DevopsCiJobRecordDTO>> entry : jobRecordMap.entrySet()) {
            String k = entry.getKey();
            List<DevopsCiJobRecordDTO> value = entry.getValue();
            DevopsCiStageRecordVO devopsCiStageRecordVO = new DevopsCiStageRecordVO();
            devopsCiStageRecordVO.setName(k);
            devopsCiStageRecordVO.setSequence(value.stream().min(Comparator.comparing(DevopsCiJobRecordDTO::getGitlabJobId)).get().getGitlabJobId());
            // 只返回job的最新记录
            List<DevopsCiJobRecordDTO> latestedsCiJobRecordDTOS = filterJobs(value);
            Map<String, List<DevopsCiJobRecordDTO>> statusMap = latestedsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStatus));
            calculateStageStatus(devopsCiStageRecordVO, statusMap);
            devopsCiStageRecordVOS.add(devopsCiStageRecordVO);
        }

        // stage排序
        devopsCiStageRecordVOS = devopsCiStageRecordVOS.stream().sorted(Comparator.comparing(DevopsCiStageRecordVO::getSequence)).filter(v -> v.getStatus() != null).collect(Collectors.toList());
        devopsCiPipelineRecordVO.setStageRecordVOList(devopsCiStageRecordVOS);
        return devopsCiPipelineRecordVO;
    }
}
