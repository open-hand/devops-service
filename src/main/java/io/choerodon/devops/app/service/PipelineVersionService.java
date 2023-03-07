package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.PipelineVersionDTO;

/**
 * 流水线版本表(PipelineVersion)应用服务
 *
 * @author
 * @since 2022-11-24 15:57:18
 */
public interface PipelineVersionService {

    void baseCreate(PipelineVersionDTO pipelineVersionDTO);

    PipelineVersionDTO createByPipelineId(Long pipelineId);

    void deleteByPipelineId(Long pipelineId);
}

