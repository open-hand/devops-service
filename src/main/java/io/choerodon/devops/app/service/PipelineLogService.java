package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.PipelineLogDTO;

/**
 * 流水线执行日志(PipelineLog)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:45
 */
public interface PipelineLogService {


    void deleteByPipelineId(Long pipelineId);

    PipelineLogDTO saveLog(Long pipelineId, Long jobRecordId, String log);

    String queryLastedByJobRecordId(Long jobRecordId);
}

