package io.choerodon.devops.domain.application.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.PipelineValueE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:02 2019/4/10
 * Description:
 */
public interface PipelineValueRepository {
    Page<PipelineValueE> listByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params);

    PipelineValueE createOrUpdate(PipelineValueE pipelineRecordE);

    void delete(Long valueId);

    PipelineValueE queryById(Long valueId);

    void checkName(Long projectId, String name);

    List<PipelineValueE> queryByAppIdAndEnvId(Long projectId, Long appId, Long envId);
}
