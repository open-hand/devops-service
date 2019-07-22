package io.choerodon.devops.domain.application.repository;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:11 2019/4/4
 * Description:
 */
public interface PipelineAppDeployRepository {
    PipelineAppDeployE baseCreate(PipelineAppDeployE pipelineAppDeployE);

    PipelineAppDeployE baseUpdate(PipelineAppDeployE pipelineAppDeployE);

    void baseDeleteById(Long appDelpoyId);

    PipelineAppDeployE baseQueryById(Long appDelpoyId);

    List<PipelineAppDeployE> baseQueryByAppId(Long appId);

    void baseCheckName(String name, Long envId);

    List<PipelineAppDeployE> baseQueryByValueId(Long valueId);

    List<PipelineAppDeployE> baseQueryByEnvId(Long envId);

    void baseUpdateWithInstanceId(Long instanceId);

}
