package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.PipelineDTO;
import io.choerodon.devops.api.dto.PipelineRecordDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:57 2019/4/3
 * Description:
 */
public interface PipelineService {
    Page<PipelineDTO> listByOptions(Long projectId, PageRequest pageRequest, String params);

    Page<PipelineRecordDTO> listRecords(Long projectId, Long pipelineId, PageRequest pageRequest, String params);
}
