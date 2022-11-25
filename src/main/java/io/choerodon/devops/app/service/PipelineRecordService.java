package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.PipelineRecordDTO;

/**
 * 流水线执行记录(PipelineRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:43:02
 */
public interface PipelineRecordService {


    void deleteByPipelineId(Long pipelineId);

    void baseCreate(PipelineRecordDTO pipelineRecordDTO);

}

