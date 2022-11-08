package io.choerodon.devops.app.service;

import java.util.Map;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.api.vo.CiPipelineRecordVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface CiCdPipelineRecordService {

    CiCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long recordRelId);

    void retryPipeline(Long projectId, Long pipelineRecordRelId, Long gitlabProjectId);

    void cancel(Long projectId, Long pipelineRecordRelId, Long gitlabProjectId);

    void executeNew(Long projectId, Long pipelineId, Long gitlabProjectId, String ref, Boolean tag, Map<String, String> variables);

    Page<CiPipelineRecordVO> pagingPipelineRecord(Long projectId, Long ciPipelineId, PageRequest pageable);

    void retryCdPipeline(Long projectId, Long cdPipelineRecordId, Boolean checkEnvPermission);
}
