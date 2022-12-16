package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.cd.PipelineStageRecordVO;
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;

/**
 * 流水线阶段记录(PipelineStageRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:13
 */
public interface PipelineStageRecordService {

    void deleteByPipelineId(Long pipelineId);

    void baseCreate(PipelineStageRecordDTO pipelineStageRecordDTO);

    PipelineStageRecordDTO baseQueryById(Long id);

    PipelineStageRecordDTO queryByIdForUpdate(Long id);

    List<PipelineStageRecordDTO> listByPipelineRecordId(Long pipelineRecordId);

    List<PipelineStageRecordVO> listVOByPipelineRecordId(Long pipelineRecordId);

    void baseUpdate(PipelineStageRecordDTO firstStageRecordDTO);

    void updateStatus(Long stageRecordId, PipelineStatusEnum status);

    void updateStatus(Long stageRecordId, String status);

    /**
     * 阶段中每个任务执行完成后触发，更新当前阶段状态，以及触发后续阶段或更新流水线状态
     *
     * @param stageRecordId
     */
    void updateStatus(Long stageRecordId);

    void cancelPipelineStages(Long pipelineRecordId);

    void cancelPipelineStagesByIds(Set<Long> ids);

    void updateCanceledAndFailedStatusToCreated(Long pipelineRecordId);
}

