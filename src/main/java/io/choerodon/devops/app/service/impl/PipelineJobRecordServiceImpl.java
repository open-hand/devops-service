package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.AduitStatusChangeVO;
import io.choerodon.devops.api.vo.AuditResultVO;
import io.choerodon.devops.api.vo.cd.PipelineJobRecordVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MessageCodeConstants;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineAuditRecordDTO;
import io.choerodon.devops.infra.dto.PipelineAuditUserRecordDTO;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.AuditStatusEnum;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.cd.CdAuditStatusEnum;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.PipelineJobRecordMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线任务记录(PipelineJobRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:34
 */
@Service
public class PipelineJobRecordServiceImpl implements PipelineJobRecordService {

    private static final String DEVOPS_SAVE_JOB_RECORD_FAILED = "devops.save.job.record.failed";

    private static final String DEVOPS_JOB_RECORD_STATUS_INVALID = "devops.job.record.status.invalid";
    private static final String DEVOPS_UPDATE_JOB_RECORD_FAILED = "devops.update.job.record.failed";
    private static final String DEVOPS_AUDIT_RECORD_NOT_EXIST = "devops.audit.record.not.exist";
    private static final String DEVOPS_STATUS_IS_EMPTY = "devops.status.is.empty";

    @Autowired
    private PipelineJobRecordMapper pipelineJobRecordMapper;
    @Autowired
    private PipelineRecordService pipelineRecordService;
    @Autowired
    private PipelineJobRecordService pipelineJobRecordService;
    @Autowired
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    private PipelineAuditRecordService pipelineAuditRecordService;
    @Autowired
    private PipelineAuditUserRecordService pipelineAuditUserRecordService;
    @Autowired
    private PipelineLogService pipelineLogService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private SendNotificationService sendNotificationService;

    @Override
    public List<PipelineJobRecordDTO> listPendingJobs(int number) {
        return pipelineJobRecordMapper.listPendingJobs(number);
    }

    @Override
    public PipelineJobRecordDTO baseQueryById(Long id) {
        return pipelineJobRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        pipelineAuditRecordService.deleteByPipelineId(pipelineId);
        pipelineAuditUserRecordService.deleteByPipelineId(pipelineId);
        pipelineLogService.deleteByPipelineId(pipelineId);

        PipelineJobRecordDTO pipelineJobRecordDTO = new PipelineJobRecordDTO();
        pipelineJobRecordDTO.setPipelineId(pipelineId);
        pipelineJobRecordMapper.delete(pipelineJobRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineJobRecordDTO pipelineJobRecordDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineJobRecordMapper, pipelineJobRecordDTO, DEVOPS_SAVE_JOB_RECORD_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(PipelineJobRecordDTO pipelineJobRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelineJobRecordMapper, pipelineJobRecordDTO, DEVOPS_UPDATE_JOB_RECORD_FAILED);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PipelineJobRecordDTO pipelineJobRecordDTO) {
        if (PipelineStatusEnum.PENDING.value().equals(pipelineJobRecordDTO.getStatus())
                || PipelineStatusEnum.NOT_AUDIT.value().equals(pipelineJobRecordDTO.getStatus())) {
            pipelineJobRecordDTO.setStartedDate(new Date());
        }
        if (Boolean.TRUE.equals(PipelineStatusEnum.isFinalStatus(pipelineJobRecordDTO.getStatus()))) {
            pipelineJobRecordDTO.setFinishedDate(new Date());
        }
        baseUpdate(pipelineJobRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePendingJobToRunning(Long id) {
        return pipelineJobRecordMapper.updatePendingJobToRunning(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, PipelineStatusEnum status) {
        PipelineJobRecordDTO pipelineJobRecordDTO = baseQueryById(id);

        if (PipelineStatusEnum.FAILED.equals(status)
                || PipelineStatusEnum.SUCCESS.equals(status)) {
            pipelineJobRecordDTO.setFinishedDate(new Date());
        }
        pipelineJobRecordDTO.setStatus(status.value());
        pipelineJobRecordMapper.updateByPrimaryKey(pipelineJobRecordDTO);
    }

    @Override
    public List<PipelineJobRecordDTO> listByStageRecordIdForUpdate(Long stageRecordId) {
        return pipelineJobRecordMapper.listByStageRecordIdForUpdate(stageRecordId);
    }

    @Override
    public List<PipelineJobRecordDTO> listCreatedByStageRecordIdForUpdate(Long stageRecordId) {
        return pipelineJobRecordMapper.listCreatedByStageIdForUpdate(stageRecordId);
    }

    @Override
    public List<PipelineJobRecordDTO> listByStageRecordId(Long stageRecordId) {
        Assert.notNull(stageRecordId, PipelineCheckConstant.DEVOPS_STAGE_RECORD_ID_IS_NULL);
        PipelineJobRecordDTO pipelineJobRecordDTO = new PipelineJobRecordDTO();
        pipelineJobRecordDTO.setStageRecordId(stageRecordId);

        return pipelineJobRecordMapper.select(pipelineJobRecordDTO);
    }

    @Override
    public List<PipelineJobRecordDTO> listByPipelineRecordId(Long pipelineRecordId) {
        Assert.notNull(pipelineRecordId, PipelineCheckConstant.DEVOPS_PIPELINE_RECORD_ID_IS_NULL);
        PipelineJobRecordDTO pipelineJobRecordDTO = new PipelineJobRecordDTO();
        pipelineJobRecordDTO.setPipelineRecordId(pipelineRecordId);

        return pipelineJobRecordMapper.select(pipelineJobRecordDTO);
    }

    @Override
    public List<PipelineJobRecordVO> listVOByPipelineRecordId(Long pipelineRecordId) {
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = listByPipelineRecordId(pipelineRecordId);
        if (CollectionUtils.isEmpty(pipelineJobRecordDTOS)) {
            return new ArrayList<>();
        }
        return pipelineJobRecordDTOS.stream().map(pipelineJobRecordDTO -> {
            PipelineJobRecordVO pipelineJobRecordVO = ConvertUtils.convertObject(pipelineJobRecordDTO, PipelineJobRecordVO.class);
            pipelineJobRecordVO.setStartedDate(pipelineJobRecordDTO.getStartedDate());
            pipelineJobRecordVO.setFinishedDate(pipelineJobRecordDTO.getFinishedDate());
            return pipelineJobRecordVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditResultVO auditJob(Long projectId, Long id, String result) {
        PipelineJobRecordDTO pipelineJobRecordDTO = baseQueryById(id);
        if (!PipelineStatusEnum.NOT_AUDIT.value().equals(pipelineJobRecordDTO.getStatus())) {
            throw new CommonException(DEVOPS_JOB_RECORD_STATUS_INVALID);
        }
        Long stageRecordId = pipelineJobRecordDTO.getStageRecordId();
        Long pipelineRecordId = pipelineJobRecordDTO.getPipelineRecordId();
        Long pipelineId = pipelineJobRecordDTO.getPipelineId();
        PipelineStageRecordDTO stageRecordDTO = pipelineStageRecordService.baseQueryById(stageRecordId);
        String stageName = stageRecordDTO.getName();
        Long userId = DetailsHelper.getUserDetails().getUserId();

        PipelineAuditRecordDTO pipelineAuditRecordDTO = pipelineAuditRecordService.queryByJobRecordIdForUpdate(id);

        List<PipelineAuditUserRecordDTO> pipelineAuditUserRecordDTOS = pipelineAuditUserRecordService.listByAuditRecordId(pipelineAuditRecordDTO.getId());
        Optional<PipelineAuditUserRecordDTO> auditUserRecord = pipelineAuditUserRecordDTOS
                .stream()
                .filter(v -> v.getUserId().equals(userId)
                        && AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus()))
                .findFirst();
        if (!auditUserRecord.isPresent()) {
            throw new CommonException(DEVOPS_AUDIT_RECORD_NOT_EXIST);
        }
        // 更新审核记录
        PipelineAuditUserRecordDTO pipelineAuditUserRecordDTO = auditUserRecord.get();
        pipelineAuditUserRecordDTO.setStatus(result);
        pipelineAuditUserRecordService.baseUpdate(pipelineAuditUserRecordDTO);

        // 计算审核结果
        AuditResultVO auditResultVO = new AuditResultVO();
        boolean auditFinishFlag;
        List<Long> userIds = pipelineAuditUserRecordDTOS.stream().map(PipelineAuditUserRecordDTO::getUserId).collect(Collectors.toList());
        if (Boolean.TRUE.equals(pipelineAuditRecordDTO.getCountersigned())) {
            auditResultVO.setCountersigned(1);
            auditFinishFlag = pipelineAuditUserRecordDTOS.stream().noneMatch(v -> AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus()));
            // 添加审核人员信息
            Map<Long, IamUserDTO> userDTOMap = baseServiceClientOperator.queryUsersByUserIds(userIds).stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));
            pipelineAuditUserRecordDTOS.forEach(v -> {
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
            auditFinishFlag = !pipelineAuditUserRecordDTOS.stream().allMatch(v -> AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus()));

            // 审核通过只有或签才发送通知
            if (AuditStatusEnum.PASSED.value().equals(result)) {
                sendNotificationService.sendPipelineAuditResultMassage(MessageCodeConstants.CD_PIPELINE_PASS,
                        pipelineId,
                        userIds,
                        pipelineRecordId,
                        stageName,
                        userId,
                        projectId);
            }
        }
        if (AuditStatusEnum.REFUSED.value().equals(result)) {
            sendNotificationService.sendPipelineAuditResultMassage(MessageCodeConstants.CD_PIPELINE_STOP,
                    pipelineId,
                    userIds,
                    pipelineRecordId,
                    stageName,
                    userId,
                    projectId);
        }
        // 审核结束则执行job
        if (auditFinishFlag) {
            Boolean auditSuccess;
            if (Boolean.TRUE.equals(pipelineAuditRecordDTO.getCountersigned())) {
                auditSuccess = pipelineAuditUserRecordDTOS.stream().allMatch(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus()));
            } else {
                auditSuccess = pipelineAuditUserRecordDTOS.stream().anyMatch(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus()));
            }
            if (Boolean.TRUE.equals(auditSuccess)) {
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.SUCCESS.value());
            } else {
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.STOP.value());
            }
            pipelineJobRecordService.update(pipelineJobRecordDTO);
            // 更新阶段状态
            pipelineStageRecordService.updateStatus(stageRecordId);
        }

        return auditResultVO;
    }

    @Override
    public AduitStatusChangeVO checkAuditStatus(Long projectId, Long id) {
        PipelineAuditRecordDTO pipelineAuditRecordDTO = pipelineAuditRecordService.queryByJobRecordIdForUpdate(id);

        List<PipelineAuditUserRecordDTO> pipelineAuditUserRecordDTOS = pipelineAuditUserRecordService.listByAuditRecordId(pipelineAuditRecordDTO.getId());
        AduitStatusChangeVO aduitStatusChangeVO = new AduitStatusChangeVO();
        aduitStatusChangeVO.setAuditStatusChanged(false); // 遗留代码，暂时不知道作用
        if (Boolean.FALSE.equals(pipelineAuditRecordDTO.getCountersigned())) {
            List<PipelineAuditUserRecordDTO> passedAuditUserRecordDTOS = pipelineAuditUserRecordDTOS.stream().filter(v -> CdAuditStatusEnum.PASSED.value().equals(v.getStatus())).collect(Collectors.toList());
            List<PipelineAuditUserRecordDTO> refusedAuditUserRecordDTOS = pipelineAuditUserRecordDTOS.stream().filter(v -> CdAuditStatusEnum.REFUSED.value().equals(v.getStatus())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(passedAuditUserRecordDTOS)) {
                calculatAuditUserName(passedAuditUserRecordDTOS, aduitStatusChangeVO);
                aduitStatusChangeVO.setCurrentStatus(PipelineStatus.SUCCESS.toValue());
            }
            if (!CollectionUtils.isEmpty(refusedAuditUserRecordDTOS)) {
                calculatAuditUserName(refusedAuditUserRecordDTOS, aduitStatusChangeVO);
                aduitStatusChangeVO.setCurrentStatus(PipelineStatus.STOP.toValue());
            }
        } else {
            List<PipelineAuditUserRecordDTO> notAuditUserRecordDTOS = pipelineAuditUserRecordDTOS.stream().filter(v -> CdAuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus())).collect(Collectors.toList());
            // 没有未审核的则状态改变
            if (CollectionUtils.isEmpty(notAuditUserRecordDTOS)) {
                List<PipelineAuditUserRecordDTO> refusedAuditUserRecordDTOS = pipelineAuditUserRecordDTOS.stream().filter(v -> AuditStatusEnum.REFUSED.value().equals(v.getStatus())).collect(Collectors.toList());
                // 没人拒绝则审核通过
                if (CollectionUtils.isEmpty(refusedAuditUserRecordDTOS)) {
                    calculatAuditUserName(pipelineAuditUserRecordDTOS, aduitStatusChangeVO);
                    aduitStatusChangeVO.setCurrentStatus(PipelineStatus.SUCCESS.toValue());
                } else {
                    calculatAuditUserName(pipelineAuditUserRecordDTOS, aduitStatusChangeVO);
                    aduitStatusChangeVO.setCurrentStatus(PipelineStatus.STOP.toValue());
                }
            }
        }

        return aduitStatusChangeVO;
    }

    @Override
    public List<PipelineJobRecordDTO> listByIds(List<Long> ids) {
        return pipelineJobRecordMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPipelineJobs(Long pipelineRecordId) {
        pipelineJobRecordMapper.cancelPipelineJobs(pipelineRecordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryPipelineJobs(Long pipelineRecordId) {
        pipelineJobRecordMapper.retryPipelineJobs(pipelineRecordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PipelineJobRecordDTO> listCreatedAndPendingJobsForUpdate(Long pipelineRecordId) {
        return pipelineJobRecordMapper.listCreatedAndPendingJobsForUpdate(pipelineRecordId);
    }

    @Override
    public List<PipelineJobRecordDTO> listByStatusForUpdate(Long pipelineRecordId, Set<String> statusList) {
        if (CollectionUtils.isEmpty(statusList)) {
            throw new CommonException(DEVOPS_STATUS_IS_EMPTY);
        }
        return pipelineJobRecordMapper.listByStatusForUpdate(pipelineRecordId, statusList);
    }

    @Override
    public String queryLog(Long projectId, Long id) {
        return pipelineLogService.queryLastedByJobRecordId(id);
    }

    @Override
    public List<PipelineJobRecordDTO> listRunningTaskBeforeDate(Date date) {
        return pipelineJobRecordMapper.listRunningTaskBeforeDate(date);
    }


    private void calculatAuditUserName(List<PipelineAuditUserRecordDTO> ciAuditUserRecordDTOS, AduitStatusChangeVO aduitStatusChangeVO) {

        if (!CollectionUtils.isEmpty(ciAuditUserRecordDTOS)) {
            aduitStatusChangeVO.setAuditStatusChanged(true);
            List<Long> userIds = ciAuditUserRecordDTOS.stream().map(PipelineAuditUserRecordDTO::getUserId).collect(Collectors.toList());

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

