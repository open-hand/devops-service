package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.cd.PipelineStageVO;
import io.choerodon.devops.infra.dto.PipelineStageDTO;

/**
 * 流水线阶段表(PipelineStage)应用服务
 *
 * @author
 * @since 2022-11-24 15:52:48
 */
public interface PipelineStageService {

    void baseCreate(PipelineStageDTO pipelineStageDTO);

    void saveStage(Long projectId, Long pipelineId, Long versionId, PipelineStageVO stage);

    void deleteByPipelineId(Long pipelineId);

    List<PipelineStageDTO> listByVersionId(Long versionId);
}

