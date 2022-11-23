package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;

/**
 * 流水线任务记录(PipelineJobRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:34
 */
public interface PipelineJobRecordService {


    List<PipelineJobRecordDTO> listPendingJobs(int number);
}

