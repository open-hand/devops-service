package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;

public interface CiCdPipelineRecordService {

    CiCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId, Long pipelineRecordId);

    void retryPipeline(Long projectId, Long pipelineRecordId, Long gitlabPipelineId, Long gitlabProjectId);
}
