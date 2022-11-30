package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.AduitStatusChangeVO;
import io.choerodon.devops.api.vo.AuditResultVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineAuditRecordDTO;
import io.choerodon.devops.infra.dto.PipelineAuditUserRecordDTO;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.enums.AuditStatusEnum;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.mapper.PipelineJobRecordMapper;
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
    private static final String DEVOPS_UPDATE_JOB_RECORD_FAILED = "devops.update.job.record.failed";

    @Autowired
    private PipelineJobRecordMapper pipelineJobRecordMapper;
    @Autowired
    private PipelineRecordService pipelineRecordService;
    @Autowired
    private PipelineAuditRecordService pipelineAuditRecordService;
    @Autowired
    private PipelineAuditUserRecordService pipelineAuditUserRecordService;
    @Autowired
    private PipelineLogService pipelineLogService;

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

        pipelineJobRecordMapper.updateByPrimaryKey(pipelineJobRecordDTO);
    }

    @Override
    public List<PipelineJobRecordDTO> listByStageRecordIdForUpdate(Long stageRecordId) {
        return pipelineJobRecordMapper.listByStageIdForUpdate(stageRecordId);
    }

    @Override
    public List<PipelineJobRecordDTO> listByStageRecordId(Long stageRecordId) {
        Assert.notNull(stageRecordId, PipelineCheckConstant.DEVOPS_STAGE_RECORD_ID_IS_NULL);
        PipelineJobRecordDTO pipelineJobRecordDTO = new PipelineJobRecordDTO();
        pipelineJobRecordDTO.setStageRecordId(stageRecordId);

        return pipelineJobRecordMapper.select(pipelineJobRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditResultVO auditJob(Long projectId, Long id, String result) {
        PipelineJobRecordDTO pipelineJobRecordDTO = baseQueryById(id);
        Long userId = DetailsHelper.getUserDetails().getUserId();
        Long pipelineRecordId = pipelineJobRecordDTO.getPipelineRecordId();

        PipelineAuditRecordDTO pipelineAuditRecordDTO = pipelineAuditRecordService.queryByJobRecordId(id);
//        String name = devopsCiJobRecordDTO.getName();

//        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = .queryById(ciPipelineRecordId);
//        Long gitlabPipelineId = devopsCiPipelineRecordDTO.getGitlabPipelineId();


//        CiAuditRecordDTO ciAuditRecordDTO = pipelineAuditRecordService.queryByUniqueOptionForUpdate(appServiceId, gitlabPipelineId, name);
        List<PipelineAuditUserRecordDTO> pipelineAuditUserRecordDTOS = pipelineAuditUserRecordService.listByAuditRecordId(pipelineAuditRecordDTO.getId());
//        List<CiAuditUserRecordDTO> ciAuditUserRecordDTOS = ciAuditUserRecordService.listByAuditRecordId(ciAuditRecordDTO.getId());
        Optional<PipelineAuditUserRecordDTO> auditUserRecord = pipelineAuditUserRecordDTOS
                .stream()
                .filter(v -> v.getUserId().equals(userId)
                        && AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus()))
                .findFirst();
//        if (!auditUserRecord.isPresent()) {
//            throw new CommonException(DEVOPS_AUDIT_RECORD_NOT_EXIST);
//        }
//        // 更新审核记录
//        CiAuditUserRecordDTO ciAuditUserRecordDTO = auditUserRecord.get();
//        ciAuditUserRecordDTO.setStatus(result);
//        ciAuditUserRecordService.baseUpdate(ciAuditUserRecordDTO);
//
//        // 计算审核结果
//        AuditResultVO auditResultVO = new AuditResultVO();
//        boolean auditFinishFlag;
//        List<Long> userIds = ciAuditUserRecordDTOS.stream().map(CiAuditUserRecordDTO::getUserId).collect(Collectors.toList());
//        if (ciAuditRecordDTO.getCountersigned()) {
//            auditResultVO.setCountersigned(1);
//            auditFinishFlag = ciAuditUserRecordDTOS.stream().noneMatch(v -> AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus()));
//            // 添加审核人员信息
//            Map<Long, IamUserDTO> userDTOMap = baseServiceClientOperator.queryUsersByUserIds(userIds).stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));
//            ciAuditUserRecordDTOS.forEach(v -> {
//                if (AuditStatusEnum.PASSED.value().equals(v.getStatus())) {
//                    IamUserDTO iamUserDTO = userDTOMap.get(v.getUserId());
//                    if (iamUserDTO != null) {
//                        auditResultVO.getAuditedUserNameList().add(iamUserDTO.getRealName());
//                    }
//                } else if (AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus())) {
//                    IamUserDTO iamUserDTO = userDTOMap.get(v.getUserId());
//                    if (iamUserDTO != null) {
//                        auditResultVO.getNotAuditUserNameList().add(iamUserDTO.getRealName());
//                    }
//                }
//            });
//
//        } else {
//            auditResultVO.setCountersigned(0);
//            auditFinishFlag = !ciAuditUserRecordDTOS.stream().allMatch(v -> AuditStatusEnum.NOT_AUDIT.value().equals(v.getStatus()));
//
//            // 审核通过只有或签才发送通知
//            if (AuditStatusEnum.PASSED.value().equals(result)) {
//                sendNotificationService.sendPipelineAuditResultMassage(MessageCodeConstants.PIPELINE_PASS,
//                        devopsCiPipelineRecordDTO.getCiPipelineId(),
//                        userIds,
//                        ciPipelineRecordId,
//                        devopsCiJobRecordDTO.getStage(),
//                        userId,
//                        projectId);
//            }
//        }
//        if (AuditStatusEnum.REFUSED.value().equals(result)) {
//            sendNotificationService.sendPipelineAuditResultMassage(MessageCodeConstants.PIPELINE_STOP,
//                    devopsCiPipelineRecordDTO.getCiPipelineId(),
//                    userIds,
//                    ciPipelineRecordId,
//                    devopsCiJobRecordDTO.getStage(),
//                    userId,
//                    projectId);
//        }
//        // 审核结束则执行job
//        if (auditFinishFlag) {
//            gitlabServiceClientOperator.playJob(TypeUtil.objToInteger(gitlabProjectId),
//                    TypeUtil.objToInteger(gitlabJobId),
//                    null,
//                    null);
//        }

//        return auditResultVO;
        return null;
    }

    @Override
    public AduitStatusChangeVO checkAuditStatus(Long projectId, Long id) {
        return null;
    }
}

