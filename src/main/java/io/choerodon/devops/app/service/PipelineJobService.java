package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineJobDTO;

/**
 * 流水线任务表(PipelineJob)应用服务
 *
 * @author
 * @since 2022-11-24 15:55:45
 */
public interface PipelineJobService {

    void baseCreate(PipelineJobDTO pipelineJobDTO);

    void deleteByPipelineId(Long pipelineId);

    List<PipelineJobDTO> listByPipelineId(Long pipelineId);

    List<PipelineJobDTO> listByVersionId(Long versionId);

    List<PipelineJobDTO> listByStageId(Long stageId);

    PipelineJobDTO baseQueryById(Long id);
}

