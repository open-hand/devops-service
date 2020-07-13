package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface CiCdPipelineRecordService {

    CiCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId);

    void retryPipeline(Long projectId, Long pipelineRecordId, Long gitlabPipelineId, Long gitlabProjectId);

    void cancel(Long projectId, Long pipelineRecordId, Long gitlabPipelineId, Long gitlabProjectId);

    void executeNew(Long projectId, Long pipelineId, Long gitlabProjectId, String ref);

    Page<CiCdPipelineRecordVO> pagingPipelineRecord(Long projectId, Long ciPipelineId, PageRequest pageable);
}
