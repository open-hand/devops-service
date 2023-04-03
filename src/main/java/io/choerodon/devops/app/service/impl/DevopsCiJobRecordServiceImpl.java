package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_GITLAB_JOB_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.AduitStatusChangeVO;
import io.choerodon.devops.api.vo.AuditResultVO;
import io.choerodon.devops.api.vo.JobWebHookVO;
import io.choerodon.devops.app.eventhandler.pipeline.job.AbstractJobHandler;
import io.choerodon.devops.app.eventhandler.pipeline.job.JobOperator;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.AuditStatusEnum;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiJobRecordMapper;
import io.choerodon.devops.infra.util.CiCdPipelineUtils;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:27
 */
@Service
public class DevopsCiJobRecordServiceImpl implements DevopsCiJobRecordService {

    private static final String DEVOPS_AUDIT_RECORD_NOT_EXIST = "devops.audit.record.not.exist";

    private static final String DEVOPS_AUDIT_RECORD_STATUS_INVALID = "devops.audit.record.status.invalid";
    private static final String DEVOPS_JOB_RECORD_CREATE = "devops.job.record.create";
    private static final String DEVOPS_JOB_RECORD_UPDATE = "devops.job.record.update";
    private static final String PIPELINE_LINK_URL_TEMPLATE = "/#/devops/pipeline-manage?type=project&id=%s&name=%s&organizationId=%s&pipelineId=%s&pipelineIdRecordId=%s";
    @Value(value = "${services.front.url: http://app.example.com}")
    private String frontUrl;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private CiAuditRecordService ciAuditRecordService;
    @Autowired
    private CiAuditUserRecordService ciAuditUserRecordService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsCiJobRecordMapper devopsCiJobRecordMapper;
    @Autowired
    @Lazy
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    @Autowired
    @Lazy
    private DevopsCiJobService devopsCiJobService;
    @Autowired
    private DevopsCiApiTestInfoService devopsCiApiTestInfoService;
    @Autowired
    @Lazy
    private JobOperator jobOperator;

    @Override
    public DevopsCiJobRecordDTO queryByAppServiceIdAndGitlabJobId(Long appServiceId, Long gitlabJobId) {
        Assert.notNull(appServiceId, ResourceCheckConstant.DEVOPS_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(gitlabJobId, DEVOPS_GITLAB_JOB_ID_IS_NULL);

        DevopsCiJobRecordDTO devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
        devopsCiJobRecordDTO.setGitlabJobId(gitlabJobId);
        devopsCiJobRecordDTO.setAppServiceId(appServiceId);
        return devopsCiJobRecordMapper.selectOne(devopsCiJobRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(JobWebHookVO jobWebHookVO, String token) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = queryByAppServiceIdAndGitlabJobId(appServiceDTO.getId(), jobWebHookVO.getBuildId());
        if (devopsCiJobRecordDTO != null) {
            devopsCiJobRecordDTO.setStatus(jobWebHookVO.getBuildStatus());
            devopsCiJobRecordDTO.setStartedDate(jobWebHookVO.getBuildStartedAt());
            devopsCiJobRecordDTO.setFinishedDate(jobWebHookVO.getBuildFinishedAt());
            devopsCiJobRecordDTO.setDurationSeconds(jobWebHookVO.getBuildDuration());
            devopsCiJobRecordMapper.updateByPrimaryKeySelective(devopsCiJobRecordDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(DevopsCiJobRecordDTO devopsCiJobRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiJobRecordMapper, devopsCiJobRecordDTO, DEVOPS_JOB_RECORD_UPDATE);
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(DEVOPS_PIPELINE_ID_IS_NULL);
        }
        // 查询流水线记录
        List<DevopsCiPipelineRecordDTO> devopsCiPipelineRecordDTOS = devopsCiPipelineRecordService.queryByPipelineId(ciPipelineId);
        if (!CollectionUtils.isEmpty(devopsCiPipelineRecordDTOS)) {
            devopsCiPipelineRecordDTOS.forEach(devopsCiPipelineRecordDTO -> {
                // 根据流水线记录id，删除job记录
                DevopsCiJobRecordDTO devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
                devopsCiJobRecordDTO.setCiPipelineRecordId(devopsCiPipelineRecordDTO.getId());
                devopsCiJobRecordMapper.delete(devopsCiJobRecordDTO);
            });
        }

    }

    @Override
    @Transactional
    public void deleteByAppServiceId(Long appServiceId) {
        Objects.requireNonNull(appServiceId);
        DevopsCiJobRecordDTO jobRecordDTO = new DevopsCiJobRecordDTO();
        jobRecordDTO.setAppServiceId(appServiceId);
        devopsCiJobRecordMapper.delete(jobRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long ciPipelineRecordId,
                       Long gitlabProjectId,
                       List<JobDTO> jobDTOS,
                       Long iamUserId,
                       Long appServiceId) {
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(devopsCiPipelineRecordDTO.getCiPipelineId());
        Map<String, DevopsCiJobDTO> jobMap = devopsCiJobDTOS.stream()
                .filter(Boolean.TRUE::equals)
                .collect(Collectors.toMap(DevopsCiJobDTO::getName, v -> v));

        for (JobDTO jobDTO : jobDTOS) {
            DevopsCiJobDTO devopsCiJobDTO = CiCdPipelineUtils.judgeAndGetJob(jobDTO.getName(), jobMap);
            if (devopsCiJobDTO == null) {
                return;
            }
            create(devopsCiPipelineRecordDTO.getCiPipelineId(), ciPipelineRecordId, gitlabProjectId, jobDTO, iamUserId, appServiceId, jobMap);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long ciPipelineRecordId,
                       Long gitlabProjectId,
                       JobDTO jobDTO,
                       Long iamUserId,
                       Long appServiceId) {
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(devopsCiPipelineRecordDTO.getCiPipelineId());
        Map<String, DevopsCiJobDTO> jobMap = devopsCiJobDTOS.stream().collect(Collectors.toMap(DevopsCiJobDTO::getName, v -> v));

        create(devopsCiPipelineRecordDTO.getCiPipelineId(), ciPipelineRecordId, gitlabProjectId, jobDTO, iamUserId, appServiceId, jobMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long ciPipelineId,
                       Long ciPipelineRecordId,
                       Long gitlabProjectId,
                       JobDTO jobDTO,
                       Long iamUserId,
                       Long appServiceId,
                       Map<String, DevopsCiJobDTO> jobMap) {
        DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
        recordDTO.setCiPipelineRecordId(ciPipelineRecordId);
        recordDTO.setGitlabProjectId(gitlabProjectId);
        recordDTO.setStatus(jobDTO.getStatus().toValue());
        recordDTO.setStage(jobDTO.getStage());
        recordDTO.setGitlabJobId(TypeUtil.objToLong(jobDTO.getId()));
        recordDTO.setStartedDate(jobDTO.getStartedAt());
        recordDTO.setFinishedDate(jobDTO.getFinishedAt());
        recordDTO.setName(jobDTO.getName());
        recordDTO.setTriggerUserId(iamUserId);
        recordDTO.setAppServiceId(appServiceId);
        DevopsCiJobDTO existDevopsCiJobDTO = CiCdPipelineUtils.judgeAndGetJob(jobDTO.getName(), jobMap);
        if (!CollectionUtils.isEmpty(jobMap) && existDevopsCiJobDTO != null) {
            recordDTO.setType(existDevopsCiJobDTO.getType());
            recordDTO.setGroupType(existDevopsCiJobDTO.getGroupType());
//            DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO();
//            devopsCiMavenSettingsDTO.setCiJobId(existDevopsCiJobDTO.getId());
//            DevopsCiMavenSettingsDTO ciMavenSettingsDTO = devopsCiMavenSettingsMapper.selectOne(devopsCiMavenSettingsDTO);
//            if (!Objects.isNull(ciMavenSettingsDTO)) {
//                recordDTO.setMavenSettingId(ciMavenSettingsDTO.getId());
//            }
        }
        baseCreate(recordDTO);
        if (!CollectionUtils.isEmpty(jobMap) && existDevopsCiJobDTO != null) {
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);
            AbstractJobHandler handler = jobOperator.getHandler(existDevopsCiJobDTO.getType());
            if (handler != null) {
                handler.saveAdditionalRecordInfo(ciPipelineId,
                        recordDTO,
                        devopsCiPipelineRecordDTO.getGitlabPipelineId(),
                        existDevopsCiJobDTO);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(DevopsCiJobRecordDTO devopsCiJobRecordDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiJobRecordMapper, devopsCiJobRecordDTO, DEVOPS_JOB_RECORD_CREATE);
    }

    @Override
    public int selectCountByCiPipelineRecordId(Long ciPipelineRecordId) {
        DevopsCiJobRecordDTO condition = new DevopsCiJobRecordDTO();
        condition.setCiPipelineRecordId(Objects.requireNonNull(ciPipelineRecordId));
        return devopsCiJobRecordMapper.selectCount(condition);
    }

    @Override
    public List<DevopsCiJobRecordDTO> listByCiPipelineRecordId(Long ciPipelineRecordId) {
        DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
        recordDTO.setCiPipelineRecordId(ciPipelineRecordId);
        return devopsCiJobRecordMapper.select(recordDTO);
    }

    @Override
    public DevopsCiJobRecordDTO baseQueryById(Long id) {
        return devopsCiJobRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditResultVO auditJob(Long projectId, Long jobRecordId, String result) {
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = baseQueryById(jobRecordId);
        if (!PipelineStatus.MANUAL.toValue().equals(devopsCiJobRecordDTO.getStatus())) {
            throw new CommonException(DEVOPS_AUDIT_RECORD_STATUS_INVALID);
        }
        Long userId = DetailsHelper.getUserDetails().getUserId();
        Long ciPipelineRecordId = devopsCiJobRecordDTO.getCiPipelineRecordId();
        Long gitlabProjectId = devopsCiJobRecordDTO.getGitlabProjectId();
        Long gitlabJobId = devopsCiJobRecordDTO.getGitlabJobId();
        Long appServiceId = devopsCiJobRecordDTO.getAppServiceId();
        String name = devopsCiJobRecordDTO.getName();

        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);
        Long gitlabPipelineId = devopsCiPipelineRecordDTO.getGitlabPipelineId();

        CiAuditRecordDTO ciAuditRecordDTO = ciAuditRecordService.queryByUniqueOptionForUpdate(appServiceId, gitlabPipelineId, name);

        List<CiAuditUserRecordDTO> ciAuditUserRecordDTOS = ciAuditUserRecordService.listByAuditRecordId(ciAuditRecordDTO.getId());
        Optional<CiAuditUserRecordDTO> auditUserRecord = ciAuditUserRecordDTOS
                .stream()
                .filter(v -> v.getUserId().equals(userId)
                        && AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus()))
                .findFirst();
        if (!auditUserRecord.isPresent()) {
            throw new CommonException(DEVOPS_AUDIT_RECORD_NOT_EXIST);
        }
        // 更新审核记录
        CiAuditUserRecordDTO ciAuditUserRecordDTO = auditUserRecord.get();
        ciAuditUserRecordDTO.setStatus(result);
        ciAuditUserRecordService.baseUpdate(ciAuditUserRecordDTO);

        // 计算审核结果
        AuditResultVO auditResultVO = new AuditResultVO();
        boolean auditFinishFlag;
        List<Long> userIds = ciAuditUserRecordDTOS.stream().map(CiAuditUserRecordDTO::getUserId).collect(Collectors.toList());
        if (ciAuditRecordDTO.getCountersigned()) {
            auditResultVO.setCountersigned(1);
            if (ciAuditUserRecordDTOS.stream().anyMatch(v -> AuditStatusEnum.REFUSED.value().equals(v.getStatus()))
                    || ciAuditUserRecordDTOS.stream().allMatch(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus()))) {
                auditFinishFlag = true;
            } else {
                auditFinishFlag = false;
            }
            // 添加审核人员信息
            Map<Long, IamUserDTO> userDTOMap = baseServiceClientOperator.queryUsersByUserIds(userIds).stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));
            ciAuditUserRecordDTOS.forEach(v -> {
                if (AuditStatusEnum.PASSED.value().equals(v.getStatus())) {
                    IamUserDTO iamUserDTO = userDTOMap.get(v.getUserId());
                    if (iamUserDTO != null) {
                        auditResultVO.getAuditedUserNameList().add(iamUserDTO.getRealName());
                    }
                } else if (AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus())) {
                    IamUserDTO iamUserDTO = userDTOMap.get(v.getUserId());
                    if (iamUserDTO != null) {
                        auditResultVO.getNotAuditUserNameList().add(iamUserDTO.getRealName());
                    }
                }
            });

        } else {
            auditResultVO.setCountersigned(0);
            auditFinishFlag = !ciAuditUserRecordDTOS.stream().allMatch(v -> AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus()));

            // 审核通过只有或签才发送通知
            if (AuditStatusEnum.PASSED.value().equals(result)) {
                sendNotificationService.sendCiPipelineAuditResultMassage(MessageCodeConstants.PIPELINE_PASS,
                        devopsCiPipelineRecordDTO.getCiPipelineId(),
                        userIds,
                        ciPipelineRecordId,
                        devopsCiJobRecordDTO.getStage(),
                        userId,
                        projectId);
            }
        }
        if (AuditStatusEnum.REFUSED.value().equals(result)) {
            sendNotificationService.sendCiPipelineAuditResultMassage(MessageCodeConstants.PIPELINE_STOP,
                    devopsCiPipelineRecordDTO.getCiPipelineId(),
                    userIds,
                    ciPipelineRecordId,
                    devopsCiJobRecordDTO.getStage(),
                    userId,
                    projectId);
        }
        // 审核结束则执行job
        if (auditFinishFlag) {
            gitlabServiceClientOperator.playJob(TypeUtil.objToInteger(gitlabProjectId),
                    TypeUtil.objToInteger(gitlabJobId),
                    null,
                    null);
        }

        return auditResultVO;
    }

    @Override
    public AduitStatusChangeVO checkAuditStatus(Long projectId, Long id) {
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = baseQueryById(id);

        Long ciPipelineRecordId = devopsCiJobRecordDTO.getCiPipelineRecordId();
        Long appServiceId = devopsCiJobRecordDTO.getAppServiceId();
        String name = devopsCiJobRecordDTO.getName();

        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);
        Long gitlabPipelineId = devopsCiPipelineRecordDTO.getGitlabPipelineId();


        CiAuditRecordDTO ciAuditRecordDTO = ciAuditRecordService.queryByUniqueOption(appServiceId, gitlabPipelineId, name);

        List<CiAuditUserRecordDTO> ciAuditUserRecordDTOS = ciAuditUserRecordService.listByAuditRecordId(ciAuditRecordDTO.getId());
        AduitStatusChangeVO aduitStatusChangeVO = new AduitStatusChangeVO();
        aduitStatusChangeVO.setAuditStatusChanged(false); // 遗留代码，暂时不知道作用
        if (!ciAuditRecordDTO.getCountersigned()) {
            List<CiAuditUserRecordDTO> passedAuditUserRecordDTOS = ciAuditUserRecordDTOS.stream().filter(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus())).collect(Collectors.toList());
            List<CiAuditUserRecordDTO> refusedAuditUserRecordDTOS = ciAuditUserRecordDTOS.stream().filter(v -> AuditStatusEnum.REFUSED.value().equals(v.getStatus())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(passedAuditUserRecordDTOS)) {
                calculatAuditUserName(passedAuditUserRecordDTOS, aduitStatusChangeVO);
                aduitStatusChangeVO.setCurrentStatus(PipelineStatus.SUCCESS.toValue());
            }
            if (!CollectionUtils.isEmpty(refusedAuditUserRecordDTOS)) {
                calculatAuditUserName(refusedAuditUserRecordDTOS, aduitStatusChangeVO);
                aduitStatusChangeVO.setCurrentStatus(PipelineStatus.STOP.toValue());
            }
        } else {
            List<CiAuditUserRecordDTO> notAuditUserRecordDTOS = ciAuditUserRecordDTOS.stream().filter(v -> AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus())).collect(Collectors.toList());
            // 没有未审核的则状态改变
            if (CollectionUtils.isEmpty(notAuditUserRecordDTOS)) {
                List<CiAuditUserRecordDTO> refusedAuditUserRecordDTOS = ciAuditUserRecordDTOS.stream().filter(v -> AuditStatusEnum.REFUSED.value().equals(v.getStatus())).collect(Collectors.toList());
                // 没人拒绝则审核通过
                if (CollectionUtils.isEmpty(refusedAuditUserRecordDTOS)) {
                    calculatAuditUserName(ciAuditUserRecordDTOS, aduitStatusChangeVO);
                    aduitStatusChangeVO.setCurrentStatus(PipelineStatus.SUCCESS.toValue());
                } else {
                    calculatAuditUserName(ciAuditUserRecordDTOS, aduitStatusChangeVO);
                    aduitStatusChangeVO.setCurrentStatus(PipelineStatus.STOP.toValue());
                }
            }
        }

        return aduitStatusChangeVO;
    }

    @Override
    public void updateApiTestTaskRecordInfo(String token, Long gitlabJobId, Long configId, Long apiTestTaskRecordId) {
        devopsCiJobRecordMapper.updateApiTestTaskRecordInfo(token, gitlabJobId, configId, apiTestTaskRecordId);
    }

    @Override
    public DevopsCiJobRecordDTO syncJobRecord(Long gitlabJobId, Long appServiceId, Long ciPipelineRecordId, Long ciPipelineId, Integer gitlabProjectId) {
        DevopsCiJobRecordDTO devopsCiJobRecordDTO;
        JobDTO jobDTO = gitlabServiceClientOperator.queryJob(gitlabProjectId, TypeUtil.objToInteger(gitlabJobId));
        DevopsCiJobDTO devopsCiJobDTO = devopsCiJobService.queryByCiPipelineIdAndName(ciPipelineId, jobDTO.getName());

        devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
        devopsCiJobRecordDTO.setGitlabJobId(gitlabJobId);
        devopsCiJobRecordDTO.setCiPipelineRecordId(ciPipelineRecordId);
        devopsCiJobRecordDTO.setStartedDate(jobDTO.getStartedAt());
        devopsCiJobRecordDTO.setFinishedDate(jobDTO.getFinishedAt());
        devopsCiJobRecordDTO.setStage(jobDTO.getStage());
        devopsCiJobRecordDTO.setType(devopsCiJobDTO.getType());
        devopsCiJobRecordDTO.setGroupType(devopsCiJobDTO.getGroupType());
        devopsCiJobRecordDTO.setName(jobDTO.getName());
        devopsCiJobRecordDTO.setStatus(jobDTO.getStatus().toString());
        devopsCiJobRecordDTO.setTriggerUserId(userAttrService.getIamUserIdByGitlabUserName(jobDTO.getUser().getUsername()));
        devopsCiJobRecordDTO.setGitlabProjectId(TypeUtil.objToLong(gitlabProjectId));
        devopsCiJobRecordDTO.setAppServiceId(appServiceId);
        return devopsCiJobRecordDTO;
    }

    @Override
    public Long checkAndGetTriggerUserId(String token, Long gitlabJobId) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = queryByAppServiceIdAndGitlabJobId(appServiceDTO.getId(), gitlabJobId);
        if (appServiceDTO.getGitlabProjectId() != devopsCiJobRecordDTO.getGitlabProjectId().intValue()) {
            throw new CommonException(ExceptionConstants.PublicCode.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        }
        return devopsCiJobRecordDTO.getTriggerUserId();
    }

    @Override
    public void testResultNotify(String token, Long gitlabJobId, String successRate) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = queryByAppServiceIdAndGitlabJobId(appServiceDTO.getId(), gitlabJobId);
        DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO = devopsCiApiTestInfoService.selectById(devopsCiJobRecordDTO.getConfigId());
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryByIdWithPipelineName(devopsCiJobRecordDTO.getCiPipelineRecordId());

        if (devopsCiApiTestInfoDTO.getEnableWarningSetting()) {
            Map<String, String> param = new HashMap<>();
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
            Tenant tenant = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            String link = String.format(PIPELINE_LINK_URL_TEMPLATE, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), devopsCiPipelineRecordDTO.getCiPipelineId(), devopsCiPipelineRecordDTO.getId());
            param.put("projectName", projectDTO.getName());
            param.put("tenantName", tenant.getTenantName());
            param.put("pipelineName", devopsCiPipelineRecordDTO.getPipelineName());
            param.put("taskName", devopsCiJobRecordDTO.getName());
            param.put("successRate", successRate);
            param.put("threshold", devopsCiApiTestInfoDTO.getPerformThreshold().toString());
            param.put("link", frontUrl + link);
            param.put("link_web", link);
            Set<Long> userIds = Arrays.stream(devopsCiApiTestInfoDTO.getNotifyUserIds().split(",")).sorted().filter(ObjectUtils::isNotEmpty).map(Long::parseLong).collect(Collectors.toSet());
            sendNotificationService.sendApiTestSuiteWarningMessage(userIds, param, devopsCiApiTestInfoDTO.getProjectId());
        }
    }

    private void calculatAuditUserName(List<CiAuditUserRecordDTO> ciAuditUserRecordDTOS, AduitStatusChangeVO aduitStatusChangeVO) {

        if (!CollectionUtils.isEmpty(ciAuditUserRecordDTOS)) {
            aduitStatusChangeVO.setAuditStatusChanged(true);
            List<Long> userIds = ciAuditUserRecordDTOS.stream().map(CiAuditUserRecordDTO::getUserId).collect(Collectors.toList());

            List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
            List<String> userNameList = new ArrayList<>();
            iamUserDTOS.forEach(iamUserDTO -> {
                if (Boolean.TRUE.equals(iamUserDTO.getLdap())) {
                    userNameList.add(iamUserDTO.getLoginName());
                } else {
                    userNameList.add(iamUserDTO.getEmail());
                }
            });
            aduitStatusChangeVO.setAuditUserName(StringUtils.join(userNameList, ","));
        }
    }
}
