package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.PipelineAppDeployValueE;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:12 2019/4/8
 * Description:
 */
public interface PipelineAppDeployValueRepository {
    PipelineAppDeployValueE create(PipelineAppDeployValueE appDeployValueE);

    PipelineAppDeployValueE update(PipelineAppDeployValueE appDeployValueE);

    PipelineAppDeployValueE queryById(Long valueId);

    List<PipelineAppDeployValueE> queryByValueId(Long pipelineValueId);
}
