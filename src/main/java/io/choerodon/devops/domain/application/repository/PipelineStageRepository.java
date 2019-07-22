package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.infra.dto.PipelineStageDTO;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:39 2019/4/8
 * Description:
 */
public interface PipelineStageRepository {

    PipelineStageDTO baseCreate(PipelineStageDTO pipelineStageDTO);

    PipelineStageDTO baseUpdate(PipelineStageDTO pipelineStageDTO);

    List<PipelineStageDTO> baseListByPipelineId(Long pipelineId);

    void baseDelete(Long stageId);

    PipelineStageDTO baseQueryById(Long stageId);
}
