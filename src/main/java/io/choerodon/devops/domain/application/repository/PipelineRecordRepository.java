package io.choerodon.devops.domain.application.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.PipelineRecordE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:25 2019/4/4
 * Description:
 */
public interface PipelineRecordRepository {
    Page<PipelineRecordE> listByOptions(Long projectId, Long pipelineId, PageRequest pageRequest, String params);

    PipelineRecordE create(PipelineRecordE pipelineRecordE);

    PipelineRecordE update(PipelineRecordE pipelineRecordE);
}
