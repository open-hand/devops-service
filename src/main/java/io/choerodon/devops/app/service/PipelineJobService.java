package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.PipelineJobDTO;

/**
 * 流水线任务表(PipelineJob)应用服务
 *
 * @author
 * @since 2022-11-24 15:55:45
 */
public interface PipelineJobService {

    void baseCreate(PipelineJobDTO pipelineJobDTO);
}

