package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.PipelineStageE;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:39 2019/4/8
 * Description:
 */
public interface PipelineStageRepository {

    PipelineStageE create(PipelineStageE pipelineStageE);

    PipelineStageE update(PipelineStageE pipelineStageE);

    List<PipelineStageE> queryByPipelineId(Long pipelineId);

    void delete(Long stageId);

    PipelineStageE queryById(Long stageId);
}
