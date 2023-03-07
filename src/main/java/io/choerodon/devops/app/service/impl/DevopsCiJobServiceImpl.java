package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.DEVOPS_GITLAB_PROJECT_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import retrofit2.Response;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.pipeline.job.JobOperator;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.enums.AppServiceEvent;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.enums.JobStatusEnum;
import io.choerodon.devops.infra.enums.sonar.SonarAuthType;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 9:24
 */
@Service
public class DevopsCiJobServiceImpl implements DevopsCiJobService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCiJobServiceImpl.class);

    private static final String DEVOPS_JOB_CREATE_FAILED = "devops.job.create.failed";
    private static final String DEVOPS_APP_SERVICE_TOKEN_MISMATCH = "devops.app.service.token.mismatch";
    private static final String DEVOPS_CI_JOB_NON_EXIST = "devops.ci.job.non.exist";
    private static final String DEVOPS_APP_SERVICE_TOKEN_PIPELINE_MISMATCH = "devops.app.service.token.pipeline.mismatch";
    private static final String DEVOPS_CI_MAVEN_SETTINGS_NOT_FOUND = "devops.ci.maven.settings.not.found";

    private static final String SONAR = "sonar";

    @Autowired
    private AppExternalConfigService appExternalConfigService;

    @Autowired
    private DevopsCiStepService devopsCiStepService;
    @Autowired
    private DevopsCiSonarConfigService devopsCiSonarConfigService;

    private DevopsCiJobMapper devopsCiJobMapper;
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    private UserAttrService userAttrService;
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;
    private AppServiceService appServiceService;
    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
    private DevopsCiPipelineService devopsCiPipelineService;
    private DevopsCiJobRecordService devopsCiJobRecordService;
    private DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;
    private AppServiceMapper appServiceMapper;
    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    private DevopsCiJobRecordMapper devopsCiJobRecordMapper;

    @Autowired
    private JobOperator jobOperator;


    public DevopsCiJobServiceImpl(DevopsCiJobMapper devopsCiJobMapper,
                                  GitlabServiceClientOperator gitlabServiceClientOperator,
                                  UserAttrService userAttrService,
                                  AppServiceService appServiceService,
                                  DevopsCiCdPipelineMapper devopsCiCdPipelineMapper,
                                  DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper,
                                  @Lazy DevopsCiPipelineService devopsCiPipelineService,
                                  @Lazy
                                          DevopsCiJobRecordService devopsCiJobRecordService,
                                  AppServiceMapper appServiceMapper,
                                  CheckGitlabAccessLevelService checkGitlabAccessLevelService,
                                  DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper,
                                  DevopsCiJobRecordMapper devopsCiJobRecordMapper) {
        this.devopsCiJobMapper = devopsCiJobMapper;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.userAttrService = userAttrService;
        this.devopsCiMavenSettingsMapper = devopsCiMavenSettingsMapper;
        this.appServiceService = appServiceService;
        this.devopsCiCdPipelineMapper = devopsCiCdPipelineMapper;
        this.devopsCiPipelineService = devopsCiPipelineService;
        this.devopsCiJobRecordService = devopsCiJobRecordService;
        this.devopsCiPipelineRecordMapper = devopsCiPipelineRecordMapper;
        this.appServiceMapper = appServiceMapper;
        this.checkGitlabAccessLevelService = checkGitlabAccessLevelService;
        this.devopsCiJobRecordMapper = devopsCiJobRecordMapper;
    }

    @Override
    @Transactional
    public DevopsCiJobDTO create(DevopsCiJobDTO devopsCiJobDTO) {
        devopsCiJobDTO.setId(null);
        if (devopsCiJobMapper.insertSelective(devopsCiJobDTO) != 1) {
            throw new CommonException(DEVOPS_JOB_CREATE_FAILED);
        }
        return devopsCiJobMapper.selectByPrimaryKey(devopsCiJobDTO.getId());
    }

    @Override
    @Transactional
    public void deleteByStageId(Long stageId) {
        if (stageId == null) {
            throw new CommonException(DEVOPS_STAGE_ID_IS_NULL);
        }

        List<Long> jobIds = listByStageId(stageId).stream().map(DevopsCiJobDTO::getId).collect(Collectors.toList());
        if (!jobIds.isEmpty()) {
            DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
            devopsCiJobDTO.setCiStageId(stageId);
            devopsCiJobMapper.delete(devopsCiJobDTO);
        }
    }

    @Override
    public void deleteByStageIdCascade(Long stageId) {
        Assert.notNull(stageId, DEVOPS_STAGE_ID_IS_NULL);

        List<Long> jobIds = listByStageId(stageId).stream().map(DevopsCiJobDTO::getId).collect(Collectors.toList());
        if (!jobIds.isEmpty()) {
            // 删除任务下的步骤
            devopsCiStepService.deleteByJobIds(jobIds);
            DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
            devopsCiJobDTO.setCiStageId(stageId);
            devopsCiJobMapper.delete(devopsCiJobDTO);
        }
    }

    @Override
    public List<DevopsCiJobDTO> listByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(DEVOPS_PIPELINE_ID_IS_NULL);
        }
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        return devopsCiJobMapper.select(devopsCiJobDTO);
    }

    @Override
    public List<DevopsCiJobVO> listCustomByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(DEVOPS_PIPELINE_ID_IS_NULL);
        }

        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        devopsCiJobDTO.setType(CiJobTypeEnum.CUSTOM.value());
        return devopsCiJobMapper.listCustomByPipelineId(ciPipelineId);
    }

    @Override
    public List<DevopsCiJobDTO> listByStageId(Long stageId) {
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiStageId(Objects.requireNonNull(stageId));
        return devopsCiJobMapper.select(devopsCiJobDTO);
    }

    @Override
    public Boolean sonarConnect(Long projectId, SonarQubeConfigVO sonarQubeConfigVO) {
        if (Objects.isNull(sonarQubeConfigVO)) {
            return false;
        }
        if (SonarAuthType.USERNAME_PWD.value().equals(sonarQubeConfigVO.getAuthType())) {
            try {
                return tryCheckSonarConnect(sonarQubeConfigVO);
            } catch (Exception e) {
                LOGGER.error("error connect :", e);
                return false;
            }
        }
        return true;
    }

    private Boolean tryCheckSonarConnect(SonarQubeConfigVO sonarQubeConfigVO) throws IOException {
        SonarClient sonarClient = RetrofitHandler.getSonarClient(
                sonarQubeConfigVO.getSonarUrl(),
                SONAR,
                sonarQubeConfigVO.getUsername(),
                sonarQubeConfigVO.getPassword());

        Response<Void> execute = sonarClient.getUser().execute();
        if (!Objects.isNull(execute.errorBody())) {
            LOGGER.error("test connect response code :{},error messsage:{}", execute.code(), execute.errorBody());
            return false;
        } else {
            return true;
        }
    }

    @Override
    public DevopsCiJobLogVO queryTrace(Long gitlabProjectId, Long jobId, Long appServiceId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId());
        //检查该用户是否有git库的权限
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
        devopsCiJobRecordDTO.setAppServiceId(appServiceId);
        devopsCiJobRecordDTO.setGitlabJobId(jobId);

        DevopsCiJobRecordDTO devopsCiJobRecordDTO1 = devopsCiJobRecordMapper.selectOne(devopsCiJobRecordDTO);
        checkGitlabAccessLevelService.checkGitlabPermission(appServiceDTO.getProjectId(), appServiceDTO.getId(), AppServiceEvent.CI_PIPELINE_DETAIL);
        String logs = gitlabServiceClientOperator.queryTrace(gitlabProjectId.intValue(), jobId.intValue(), userAttrDTO.getGitlabUserId().intValue(), appExternalConfigDTO);
        return new DevopsCiJobLogVO(JobStatusEnum.execEnd(devopsCiJobRecordDTO1.getStatus()), logs);
    }

    @Override
    public void retryJob(Long projectId, Long gitlabProjectId, Long jobId, Long appServiceId) {
        Assert.notNull(gitlabProjectId, DEVOPS_GITLAB_PROJECT_ID_IS_NULL);
        Assert.notNull(jobId, DEVOPS_GITLAB_JOB_ID_IS_NULL);
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByAppServiceIdAndGitlabJobId(appServiceId, jobId);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);

        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {

            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceDTO.getId(), AppServiceEvent.CI_PIPELINE_RETRY_TASK);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId());

        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(devopsCiJobRecordDTO.getCiPipelineRecordId());
        devopsCiPipelineService.checkUserBranchPushPermission(projectId, userAttrDTO.getGitlabUserId(), gitlabProjectId, devopsCiPipelineRecordDTO.getGitlabTriggerRef());

        JobDTO jobDTO = gitlabServiceClientOperator.retryJob(gitlabProjectId.intValue(),
                jobId.intValue(),
                userAttrDTO.getGitlabUserId().intValue(),
                appExternalConfigDTO);
        // 保存job记录
        try {
            devopsCiJobRecordService.create(devopsCiPipelineRecordDTO.getId(), gitlabProjectId, jobDTO, userAttrDTO.getIamUserId(), appServiceDTO.getId());
        } catch (Exception e) {
            LOGGER.info("update job Records failed， jobid {}.", jobId);
        }
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(DEVOPS_PIPELINE_ID_IS_NULL);
        }

        List<DevopsCiJobDTO> devopsCiJobDTOS = listByPipelineId(ciPipelineId);
        if (CollectionUtils.isEmpty(devopsCiJobDTOS)) {
            return;
        }
        List<Long> jobIds = devopsCiJobDTOS.stream().map(DevopsCiJobDTO::getId).collect(Collectors.toList());
        // 删除maven settings
        deleteMavenSettingsRecordByJobIds(jobIds);

        // 删除步骤
        devopsCiStepService.deleteByJobIds(jobIds);

        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        devopsCiJobMapper.delete(devopsCiJobDTO);
    }

    @Override
    public String queryMavenSettings(Long projectId, String appServiceToken, Long jobId, Long sequence) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(appServiceToken);
        if (appServiceDTO == null) {
            throw new DevopsCiInvalidException(DEVOPS_APP_SERVICE_TOKEN_MISMATCH);
        }
        DevopsCiJobDTO devopsCiJobDTO = devopsCiJobMapper.selectByPrimaryKey(jobId);
        if (devopsCiJobDTO == null) {
            throw new DevopsCiInvalidException(DEVOPS_CI_JOB_NON_EXIST);
        }

        CiCdPipelineDTO devopsCiPipelineDTO = devopsCiCdPipelineMapper.selectByPrimaryKey(devopsCiJobDTO.getCiPipelineId());
        if (devopsCiPipelineDTO == null || !Objects.equals(devopsCiPipelineDTO.getAppServiceId(), appServiceDTO.getId())) {
            throw new DevopsCiInvalidException(DEVOPS_APP_SERVICE_TOKEN_PIPELINE_MISMATCH);
        }

        return devopsCiMavenSettingsMapper.queryMavenSettings(jobId, sequence);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteMavenSettingsRecordByJobIds(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return;
        }
        devopsCiMavenSettingsMapper.deleteByJobIds(jobIds);
    }

    @Override
    public SonarInfoVO getSonarConfig(Long projectId, Long appServiceId, String code) {
        SonarInfoVO sonarInfoVO = new SonarInfoVO();
        if (!Objects.isNull(appServiceId)) {
            sonarInfoVO = getCiSonar(appServiceId);
        }
        if (!Objects.isNull(code)) {
            AppServiceDTO appServiceDTO = new AppServiceDTO();
            appServiceDTO.setCode(code);
            AppServiceDTO serviceDTO = appServiceMapper.selectOne(appServiceDTO);
            if (!Objects.isNull(serviceDTO)) {
                sonarInfoVO = getCiSonar(serviceDTO.getId());
            }
        }
        return sonarInfoVO;
    }

    @Override
    @Transactional
    public void playJob(Long projectId, Long gitlabProjectId, Long jobId, Long appServiceId) {
        Assert.notNull(gitlabProjectId, DEVOPS_GITLAB_PROJECT_ID_IS_NULL);
        Assert.notNull(jobId, DEVOPS_GITLAB_JOB_ID_IS_NULL);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId());
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByAppServiceIdAndGitlabJobId(appServiceId, jobId);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectByPrimaryKey(devopsCiJobRecordDTO.getCiPipelineRecordId());

        devopsCiPipelineService.checkUserBranchMergePermission(projectId, userAttrDTO.getGitlabUserId(), gitlabProjectId, devopsCiPipelineRecordDTO.getGitlabTriggerRef());


        JobDTO jobDTO = gitlabServiceClientOperator.playJob(gitlabProjectId.intValue(),
                jobId.intValue(),
                userAttrDTO.getGitlabUserId().intValue(),
                appExternalConfigDTO);

        devopsCiJobRecordDTO.setStatus(jobDTO.getStatus().toString());

        devopsCiJobRecordMapper.updateByPrimaryKeySelective(devopsCiJobRecordDTO);
    }

    @Override
    public List<DevopsCiJobDTO> listAll() {
        return devopsCiJobMapper.selectAll();
    }

    @Override
    public String queryMavenSettings(Long projectId, String token, Long id) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
        if (appServiceDTO == null) {
            throw new DevopsCiInvalidException(DEVOPS_APP_SERVICE_TOKEN_MISMATCH);
        }
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = devopsCiMavenSettingsMapper.selectByPrimaryKey(id);
        if (devopsCiMavenSettingsDTO == null) {
            throw new DevopsCiInvalidException(DEVOPS_CI_MAVEN_SETTINGS_NOT_FOUND);
        }
        return devopsCiMavenSettingsDTO.getMavenSettings();
    }

    @Override
    public Boolean doesApiTestSuiteRelatedWithPipeline(Long projectId, Long suiteId) {
        return devopsCiJobMapper.doesApiTestSuiteRelatedWithPipeline(projectId, suiteId);
    }

    @Override
    public DevopsCiJobDTO selectByPrimaryKey(Long id) {
        return devopsCiJobMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsCiJobDTO queryByCiPipelineIdAndName(Long ciPipelineId, String name) {

        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        devopsCiJobDTO.setName(name);
        return devopsCiJobMapper.selectOne(devopsCiJobDTO);

    }

    @Override
    public List<PipelineInstanceReferenceVO> listApiTestTaskReferencePipelineInfo(Long projectId, Set<Long> taskIds) {
        return devopsCiJobMapper.listApiTestTaskReferencePipelineInfo(projectId, taskIds);
    }

    @Override
    public PipelineInstanceReferenceVO queryPipelineReferenceEnvApp(Long projectId, Long appId) {
        return devopsCiJobMapper.queryPipelineReferenceEnvApp(projectId, appId);
    }

    @Override
    public PipelineInstanceReferenceVO queryChartPipelineReference(Long projectId, Long appId) {
        return devopsCiJobMapper.queryChartPipelineReference(projectId, appId);
    }

    @Override
    public PipelineInstanceReferenceVO queryDeployValuePipelineReference(Long projectId, Long valueId) {
        return devopsCiJobMapper.queryDeployValuePipelineReference(projectId, valueId);
    }

    @Override
    public PipelineInstanceReferenceVO queryPipelineReferenceHostApp(Long projectId, Long appId) {
        return devopsCiJobMapper.queryPipelineReferenceHostApp(appId);
    }

    private SonarInfoVO getCiSonar(Long appServiceId) {
        SonarInfoVO sonarInfoVO = new SonarInfoVO();
        CiCdPipelineDTO devopsCiPipelineDTO = new CiCdPipelineDTO();
        devopsCiPipelineDTO.setAppServiceId(appServiceId);
        CiCdPipelineDTO ciPipelineDTO = devopsCiCdPipelineMapper.selectOne(devopsCiPipelineDTO);
        if (!Objects.isNull(ciPipelineDTO)) {
            List<DevopsCiJobDTO> devopsCiJobDTOList = listByPipelineId(ciPipelineDTO.getId());
            List<Long> jobIds = devopsCiJobDTOList.stream().map(DevopsCiJobDTO::getId).collect(Collectors.toList());
            List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobIds(jobIds);

            Optional<DevopsCiStepDTO> first = devopsCiStepDTOS.stream().filter(v -> DevopsCiStepTypeEnum.SONAR.value().equals(v.getType())).findFirst();
            if (first.isPresent()) {
                DevopsCiStepDTO devopsCiStepDTO = first.get();
                DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = devopsCiSonarConfigService.queryByStepId(devopsCiStepDTO.getId());
                sonarInfoVO = ConvertUtils.convertObject(devopsCiSonarConfigDTO, SonarInfoVO.class);
            }
        }
        return sonarInfoVO;
    }
}
