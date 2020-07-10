package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;

public interface CiCdPipelineRecordService {

    CiCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId);

    void retryPipeline(Long projectId, Long pipelineRecordId, Long gitlabPipelineId, Long gitlabProjectId);

    void cancel(Long projectId, Long pipelineRecordId, Long gitlabPipelineId, Long gitlabProjectId);

    void executeNew(Long projectId, Long pipelineId, Long gitlabProjectId, String ref);
}
