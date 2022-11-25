package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.PipelineVO;
import io.choerodon.devops.infra.dto.PipelineDTO;

/**
 * 流水线表(Pipeline)应用服务
 *
 * @author
 * @since 2022-11-24 15:50:13
 */
public interface PipelineService {
    void baseCreate(PipelineDTO pipelineDTO);

    void baseDeleteById(Long id);

    PipelineDTO baseQueryById(Long id);

    PipelineDTO create(Long projectId, PipelineVO pipelineVO);

    void enable(Long projectId, Long id);

    void disable(Long projectId, Long id);

    void delete(Long projectId, Long id);
}

