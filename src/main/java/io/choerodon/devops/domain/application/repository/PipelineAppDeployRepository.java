package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:11 2019/4/4
 * Description:
 */
public interface PipelineAppDeployRepository {
    PipelineAppDeployE create(PipelineAppDeployE pipelineAppDeployE);

    PipelineAppDeployE update(PipelineAppDeployE pipelineAppDeployE);

    void deleteById(Long appDelpoyId);

    PipelineAppDeployE queryById(Long appDelpoyId);

    List<PipelineAppDeployE> queryByAppId(Long appId);
}
