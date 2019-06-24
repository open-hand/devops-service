package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.PipelineAppDeployE;

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

    void checkName(String name, Long envId);

    List<PipelineAppDeployE> queryByValueId(Long valueId);

    List<PipelineAppDeployE> queryByEnvId(Long envId);

    void updateInstanceId(Long instanceId);

}
