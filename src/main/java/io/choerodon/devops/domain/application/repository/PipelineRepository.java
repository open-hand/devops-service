package io.choerodon.devops.domain.application.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.PipelineE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:19 2019/4/4
 * Description:
 */
public interface PipelineRepository {
    Page<PipelineE> listByOptions(Long projectId, PageRequest pageRequest, String params);
}
