package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsPipelineAuditVO;
import io.choerodon.devops.api.vo.cd.PipelineJobRecordVO;
import io.choerodon.devops.api.vo.cd.PipelineRecordVO;
import io.choerodon.devops.api.vo.cd.PipelineStageRecordVO;
import io.choerodon.devops.app.eventhandler.cd.AbstractCdJobHandler;
import io.choerodon.devops.app.eventhandler.cd.CdJobOperator;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.AuditStatusEnum;
import io.choerodon.devops.infra.enums.cd.CdJobTypeEnum;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.PipelineRecordMapper;
import io.choerodon.devops.infra.util.CiCdPipelineUtils;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 流水线执行记录(PipelineRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:02
 */
@Service
public class PipelineRecordServiceImpl implements PipelineRecordService {

    private static final String DEVOPS_SAVE_PIPELINE_RECORD_FAILED = "devops.save.pipeline.record.failed";
    private static final String DEVOPS_UPDATE_PIPELINE_RECORD_FAILED = "devops.update.pipeline.record.failed";

    @Autowired
    private PipelineRecordMapper pipelineRecordMapper;
    @Autowired
    @Lazy
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    @Lazy
    private PipelineJobRecordService pipelineJobRecordService;
    @Autowired
    private CdJobOperator cdJobOperator;

    @Autowired
    private PipelineAuditRecordService pipelineAuditRecordService;
    @Autowired
    private PipelineAuditUserRecordService pipelineAuditUserRecordService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO();
        pipelineRecordDTO.setPipelineId(pipelineId);
        pipelineRecordMapper.delete(pipelineRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineRecordDTO pipelineRecordDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineRecordMapper,
                pipelineRecordDTO,
                DEVOPS_SAVE_PIPELINE_RECORD_FAILED);
    }

    @Override
    public PipelineRecordDTO baseQueryById(Long pipelineRecordId) {
        return pipelineRecordMapper.selectByPrimaryKey(pipelineRecordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(PipelineRecordDTO pipelineRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelineRecordMapper,
                pipelineRecordDTO,
                DEVOPS_UPDATE_PIPELINE_RECORD_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateToEndStatus(Long pipelineRecordId, PipelineStatusEnum status) {
        pipelineRecordMapper.updateStatusToFailed(pipelineRecordId, new Date(), status.value());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        PipelineRecordDTO pipelineRecordDTO = baseQueryById(id);
        if (!pipelineRecordDTO.getStatus().equals(status)) {
            pipelineRecordDTO.setStatus(status);
            if (PipelineStatusEnum.isFinalStatus(status)) {
                pipelineRecordDTO.setFinishedDate(new Date());
            }
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelineRecordMapper, pipelineRecordDTO, DEVOPS_UPDATE_PIPELINE_RECORD_FAILED);
        }

    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void startNextStage(PipelineRecordDTO pipelineRecordDTO, PipelineStageRecordDTO firstStageRecordDTO, List<PipelineJobRecordDTO> firstJobRecordList) {
//        boolean hasAuditJob = false;
//        for (PipelineJobRecordDTO pipelineJobRecordDTO : firstJobRecordList) {
//            if (CdJobTypeEnum.AUDIT.value().equals(pipelineJobRecordDTO.getType())) {
//                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.NOT_AUDIT.value());
//                hasAuditJob = true;
//            } else {
//                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.PENDING.value());
//            }
//            pipelineJobRecordService.baseUpdate(pipelineJobRecordDTO);
//        }
//        if (Boolean.TRUE.equals(hasAuditJob)) {
//            firstStageRecordDTO.setStatus(PipelineStatusEnum.NOT_AUDIT.value());
//            pipelineRecordDTO.setStatus(PipelineStatusEnum.NOT_AUDIT.value());
//        } else {
//            firstStageRecordDTO.setStatus(PipelineStatusEnum.PENDING.value());
//            pipelineRecordDTO.setStatus(PipelineStatusEnum.RUNNING.value());
//        }
//        pipelineStageRecordService.updateStatus(firstStageRecordDTO.getId());
//        baseUpdate(pipelineRecordDTO);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startNextStage(Long nextStageRecordId) {
        PipelineStageRecordDTO pipelineStageRecordDTO = pipelineStageRecordService.baseQueryById(nextStageRecordId);
        Long pipelineRecordId = pipelineStageRecordDTO.getPipelineRecordId();
        Long pipelineId = pipelineStageRecordDTO.getPipelineId();
        String stageName = pipelineStageRecordDTO.getName();
//        PipelineRecordDTO pipelineRecordDTO = baseQueryById(pipelineStageRecordDTO.getPipelineRecordId());
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listCreatedByStageRecordIdForUpdate(nextStageRecordId);
//        startNextStage(pipelineRecordDTO, pipelineStageRecordDTO, pipelineJobRecordDTOS);
//        boolean hasAuditJob = false;
        List<PipelineJobRecordDTO> auditJobList = new ArrayList<>();
        for (PipelineJobRecordDTO pipelineJobRecordDTO : pipelineJobRecordDTOS) {
            if (CdJobTypeEnum.AUDIT.value().equals(pipelineJobRecordDTO.getType())) {
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.NOT_AUDIT.value());
//                hasAuditJob = true;
                auditJobList.add(pipelineJobRecordDTO);
            } else {
                pipelineJobRecordDTO.setStatus(PipelineStatusEnum.PENDING.value());
            }
            pipelineJobRecordService.update(pipelineJobRecordDTO);
        }

        String newStageStatus = pipelineJobRecordDTOS.stream().max(Comparator.comparing(job -> PipelineStatusEnum.getPriorityByValue(job.getStatus()))).map(PipelineJobRecordDTO::getStatus).get();
        if (!pipelineStageRecordDTO.getStatus().equals(newStageStatus)) {
            pipelineStageRecordService.updateStatus(nextStageRecordId, newStageStatus);
            List<PipelineStageRecordDTO> pipelineStageRecordDTOS = pipelineStageRecordService.listByPipelineRecordId(pipelineRecordId);
            String newPipelineStatus = pipelineStageRecordDTOS.stream().max(Comparator.comparing(job -> PipelineStatusEnum.getPriorityByValue(job.getStatus()))).map(PipelineStageRecordDTO::getStatus).get();

            updateStatus(pipelineRecordId, newPipelineStatus);
        }
        // 人工卡点任务发送审核通知
        if (!CollectionUtils.isEmpty(auditJobList)) {
            auditJobList.forEach(auditJob -> {
                pipelineAuditRecordService.sendJobAuditMessage(pipelineId, pipelineRecordId, stageName, auditJob.getId());
            });
        }


//        if (Boolean.TRUE.equals(hasAuditJob)) {
//            firstStageRecordDTO.setStatus(PipelineStatusEnum.NOT_AUDIT.value());
//            pipelineRecordDTO.setStatus(PipelineStatusEnum.NOT_AUDIT.value());
//        } else {
//            firstStageRecordDTO.setStatus(PipelineStatusEnum.PENDING.value());
//            pipelineRecordDTO.setStatus(PipelineStatusEnum.RUNNING.value());
//        }
//        pipelineStageRecordService.updateStatus(nextStageRecordId);
//        baseUpdate(pipelineRecordDTO);
    }

    @Override
    public Page<PipelineRecordVO> paging(Long projectId, Long pipelineId, PageRequest pageable) {

        Page<PipelineRecordVO> pipelineRecordVOPage = PageHelper.doPage(pageable, () -> pipelineRecordMapper.listByPipelineId(pipelineId));
        if (pipelineRecordVOPage.isEmpty()) {
            return new Page<>();
        }

        pipelineRecordVOPage.getContent().forEach(pipelineRecordVO -> {
            Long pipelineRecordId = pipelineRecordVO.getId();
            List<PipelineStageRecordVO> pipelineStageRecordDTOS = pipelineStageRecordService.listVOByPipelineRecordId(pipelineRecordId);
            List<PipelineStageRecordVO> sortedStageRecords = pipelineStageRecordDTOS.stream().sorted(Comparator.comparing(PipelineStageRecordVO::getSequence)).collect(Collectors.toList());
            pipelineRecordVO.setStageRecordList(sortedStageRecords);
            addAuditInfo(pipelineRecordVO);
            pipelineRecordVO.setViewId(CiCdPipelineUtils.handleId(pipelineRecordVO.getId()));
        });

        UserDTOFillUtil.fillUserInfo(pipelineRecordVOPage.getContent(), "createdBy", "trigger");

        return pipelineRecordVOPage;
    }

    private void addAuditInfo(PipelineRecordVO pipelineRecordVO) {
        if (PipelineStatusEnum.NOT_AUDIT.value().equals(pipelineRecordVO.getStatus())) {
            Long userId = DetailsHelper.getUserDetails().getUserId();
            List<DevopsPipelineAuditVO> pipelineAuditInfo = new ArrayList<>();
            List<PipelineAuditRecordDTO> pipelineAuditRecordDTOS = pipelineAuditRecordService.listByPipelineRecordId(pipelineRecordVO.getId());
            if (!CollectionUtils.isEmpty(pipelineAuditRecordDTOS)) {

                List<Long> jobRecordIds = pipelineAuditRecordDTOS.stream().map(PipelineAuditRecordDTO::getJobRecordId).collect(Collectors.toList());
                List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listByIds(jobRecordIds);
                Map<Long, PipelineJobRecordDTO> jobRecordDTOMap = pipelineJobRecordDTOS.stream().collect(Collectors.toMap(PipelineJobRecordDTO::getId, Function.identity()));

                pipelineAuditRecordDTOS.forEach(pipelineAuditRecordDTO -> {
                    PipelineJobRecordDTO pipelineJobRecordDTO = jobRecordDTOMap.get(pipelineAuditRecordDTO.getJobRecordId());
                    List<PipelineAuditUserRecordDTO> pipelineAuditUserRecordDTOS = pipelineAuditUserRecordService.listByAuditRecordId(pipelineAuditRecordDTO.getId());
                    if (!CollectionUtils.isEmpty(pipelineAuditUserRecordDTOS)) {
                        if (pipelineAuditUserRecordDTOS.stream().anyMatch(r -> r.getUserId().equals(userId) && AuditStatusEnum.NOT_AUDIT.value().equals(r.getStatus()))) {
                            DevopsPipelineAuditVO devopsCiPipelineAuditVO = new DevopsPipelineAuditVO(pipelineJobRecordDTO.getName(), pipelineJobRecordDTO.getId());
                            pipelineAuditInfo.add(devopsCiPipelineAuditVO);
                        }
                    }
                });
            }
            pipelineRecordVO.setPipelineAuditInfo(pipelineAuditInfo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long projectId, Long id) {
        // 更新所有created、pending状态的任务状态为canceled
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listCreatedAndPendingJobsForUpdate(id);
        if (CollectionUtils.isEmpty(pipelineJobRecordDTOS)) {
            return;
        }
        pipelineJobRecordService.cancelPipelineJobs(id);
        // 更新所有created、pending、running状态的阶段状态为canceled
        Set<Long> stageRecordIds = pipelineJobRecordDTOS.stream().map(PipelineJobRecordDTO::getStageRecordId).collect(Collectors.toSet());
        pipelineStageRecordService.cancelPipelineStagesByIds(stageRecordIds);
        // 更新流水线状态为canceled
        updateStatus(id, PipelineStatusEnum.CANCELED.value());
    }

    @Override
    public PipelineRecordVO query(Long projectId, Long id) {
        PipelineRecordDTO pipelineRecordDTO = baseQueryById(id);
        PipelineRecordVO pipelineRecordVO = ConvertUtils.convertObject(pipelineRecordDTO, PipelineRecordVO.class);

        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(pipelineRecordVO.getCreatedBy());
        pipelineRecordVO.setTrigger(iamUserDTO);

        // 查询阶段信息
        List<PipelineStageRecordVO> pipelineStageRecordVOS = pipelineStageRecordService.listVOByPipelineRecordId(id);
        List<PipelineStageRecordVO> sortedPipelineStageRecordVOS = pipelineStageRecordVOS.stream().sorted(Comparator.comparing(PipelineStageRecordVO::getSequence)).collect(Collectors.toList());

        List<PipelineJobRecordVO> pipelineJobRecordVOS = pipelineJobRecordService.listVOByPipelineRecordId(id);
        Map<Long, List<PipelineJobRecordVO>> jobRecordMap = pipelineJobRecordVOS.stream().collect(Collectors.groupingBy(PipelineJobRecordVO::getStageRecordId));
        sortedPipelineStageRecordVOS.forEach(pipelineStageRecordVO -> {
            Long stageRecordId = pipelineStageRecordVO.getId();
            List<PipelineJobRecordVO> pipelineJobRecordVOList = jobRecordMap.get(stageRecordId);
            pipelineJobRecordVOList.forEach(pipelineJobRecordVO -> {
                // 按类型填充Job信息
                AbstractCdJobHandler handler = cdJobOperator.getHandler(pipelineJobRecordVO.getType());
                handler.fillAdditionalRecordInfo(pipelineJobRecordVO);
            });

            pipelineStageRecordVO.setJobRecordList(pipelineJobRecordVOList);
        });
        addAuditInfo(pipelineRecordVO);

        pipelineRecordVO.setStageRecordList(sortedPipelineStageRecordVOS);
        return pipelineRecordVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retry(Long projectId, Long id) {
        // 更新所有canceled、failed状态的任务状态为created
        Set<String> statusList = new HashSet<>();
        statusList.add(PipelineStatusEnum.CANCELED.value());
        statusList.add(PipelineStatusEnum.FAILED.value());
        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listByStatusForUpdate(id, statusList);
        if (CollectionUtils.isEmpty(pipelineJobRecordDTOS)) {
            return;
        }
        pipelineJobRecordService.retryPipelineJobs(id);
        // 更新所有canceled状态的阶段状态为created
        pipelineStageRecordService.updateCanceledAndFailedStatusToCreated(id);
        // 更新流水线状态为created
        pipelineRecordMapper.retryPipeline(id, new Date());
        List<PipelineStageRecordDTO> pipelineStageRecordDTOS = pipelineStageRecordService.listByPipelineRecordId(id);
        List<PipelineStageRecordDTO> sortedRecordStages = pipelineStageRecordDTOS.stream().sorted(Comparator.comparing(PipelineStageRecordDTO::getSequence)).collect(Collectors.toList());
        Long nextStageId = null;
        for (PipelineStageRecordDTO stageRecordDTO : sortedRecordStages) {
            if (PipelineStatusEnum.CANCELED.value().equals(stageRecordDTO.getStatus())) {
                nextStageId = stageRecordDTO.getId();
                break;
            }
        }
        startNextStage(nextStageId);
    }

}

