package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.cd.PipelineStageRecordVO;
import io.choerodon.devops.app.service.PipelineJobRecordService;
import io.choerodon.devops.app.service.PipelineRecordService;
import io.choerodon.devops.app.service.PipelineStageRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.mapper.PipelineStageRecordMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线阶段记录(PipelineStageRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:13
 */
@Service
public class PipelineStageRecordServiceImpl implements PipelineStageRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineStageRecordServiceImpl.class);

    private static final String DEVOPS_SAVE_STAGE_RECORD_FAILED = "devops.save.stage.record.failed";
    private static final String DEVOPS_UPDATE_STAGE_RECORD_FAILED = "devops.update.stage.record.failed";

    @Autowired
    private PipelineStageRecordMapper pipelineStageRecordMapper;
    @Autowired
    @Lazy
    private PipelineRecordService pipelineRecordService;
    @Autowired
    @Lazy
    private PipelineJobRecordService pipelineJobRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineStageRecordDTO pipelineStageRecordDTO = new PipelineStageRecordDTO();
        pipelineStageRecordDTO.setPipelineId(pipelineId);
        pipelineStageRecordMapper.delete(pipelineStageRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineStageRecordDTO pipelineStageRecordDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineStageRecordMapper, pipelineStageRecordDTO, DEVOPS_SAVE_STAGE_RECORD_FAILED);

    }

    @Override
    public PipelineStageRecordDTO baseQueryById(Long id) {
        return pipelineStageRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public PipelineStageRecordDTO queryByIdForUpdate(Long id) {
        return pipelineStageRecordMapper.queryByIdForUpdate(id);
    }

    @Override
    public List<PipelineStageRecordDTO> listByPipelineRecordId(Long pipelineRecordId) {
        Assert.notNull(pipelineRecordId, PipelineCheckConstant.DEVOPS_PIPELINE_RECORD_ID_IS_NULL);

        PipelineStageRecordDTO pipelineStageRecordDTO = new PipelineStageRecordDTO();
        pipelineStageRecordDTO.setPipelineRecordId(pipelineRecordId);
        return pipelineStageRecordMapper.select(pipelineStageRecordDTO);
    }

    @Override
    public List<PipelineStageRecordVO> listVOByPipelineRecordId(Long pipelineRecordId) {
        List<PipelineStageRecordDTO> pipelineStageRecordDTOS = listByPipelineRecordId(pipelineRecordId);
        if (CollectionUtils.isEmpty(pipelineStageRecordDTOS)) {
            return new ArrayList<>();
        }
        return ConvertUtils.convertList(pipelineStageRecordDTOS, PipelineStageRecordVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(PipelineStageRecordDTO pipelineStageRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelineStageRecordMapper, pipelineStageRecordDTO, DEVOPS_UPDATE_STAGE_RECORD_FAILED);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long stageRecordId, PipelineStatusEnum status) {
        updateStatus(stageRecordId, status.value());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long stageRecordId, String status) {
        PipelineStageRecordDTO pipelineStageRecordDTO = baseQueryById(stageRecordId);

        pipelineStageRecordDTO.setStatus(status);

        pipelineStageRecordMapper.updateByPrimaryKeySelective(pipelineStageRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long stageRecordId) {
        PipelineStageRecordDTO pipelineStageRecordDTO = queryByIdForUpdate(stageRecordId);
        if (PipelineStatusEnum.CANCELED.value().equals(pipelineStageRecordDTO.getStatus())) {
            LOGGER.info("Pipeline stage:{} status is canceled, skip update it.", stageRecordId);
            return;
        }

        List<PipelineJobRecordDTO> pipelineJobRecordDTOS = pipelineJobRecordService.listByStageRecordIdForUpdate(stageRecordId);

        String newStatus = pipelineJobRecordDTOS.stream().max(Comparator.comparing(job -> PipelineStatusEnum.getPriorityByValue(job.getStatus()))).map(PipelineJobRecordDTO::getStatus).get();

        if (!newStatus.equals(pipelineStageRecordDTO.getStatus())) {
            updateStatus(stageRecordId, newStatus);
            List<PipelineStageRecordDTO> pipelineStageRecordDTOS = listByPipelineRecordId(pipelineStageRecordDTO.getPipelineRecordId());
            String newPipelineStatus = pipelineStageRecordDTOS.stream().max(Comparator.comparing(job -> PipelineStatusEnum.getPriorityByValue(job.getStatus()))).map(PipelineStageRecordDTO::getStatus).get();
            pipelineRecordService.updateStatus(pipelineStageRecordDTO.getPipelineRecordId(), newPipelineStatus);
            if (PipelineStatusEnum.SUCCESS.value().equals(newStatus)) {
                // 执行下一阶段任务
                if (pipelineStageRecordDTO.getNextStageRecordId() != null) {
                    pipelineRecordService.startNextStage(pipelineStageRecordDTO.getNextStageRecordId());
                }
//                else {
//                    // 没有下一阶段则说明流水线执行完成，更新流水线状态
//                    pipelineRecordService.updateToEndStatus(pipelineStageRecordDTO.getPipelineRecordId(), PipelineStatusEnum.SUCCESS);
//                }
            }
        }
//
//
//        if (pipelineJobRecordDTOS.stream().allMatch(job -> PipelineStatusEnum.SUCCESS.value().equals(job.getStatus()))) {
//            updateStatus(stageRecordId, PipelineStatusEnum.SUCCESS);
//
//        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPipelineStages(Long pipelineRecordId) {
        pipelineStageRecordMapper.cancelPipelineStages(pipelineRecordId);
    }
}

