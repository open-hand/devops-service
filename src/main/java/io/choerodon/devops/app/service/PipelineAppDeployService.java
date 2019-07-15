package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.PipelineAppDeployE;
import io.choerodon.devops.infra.dto.PipelineAppDeployDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:04 2019/7/15
 * Description:
 */
public interface PipelineAppDeployService {
    PipelineAppDeployDTO baseCreate(PipelineAppDeployDTO pipelineAppDeployDTO);

    PipelineAppDeployDTO baseUpdate(PipelineAppDeployDTO pipelineAppDeployDTO);

    void baseDeleteById(Long appDelpoyId);

    PipelineAppDeployDTO baseQueryById(Long appDelpoyId);

    List<PipelineAppDeployDTO> baseQueryByAppId(Long appId);

    void baseCheckName(String name, Long envId);

    List<PipelineAppDeployDTO> baseQueryByValueId(Long valueId);

    List<PipelineAppDeployDTO> baseQueryByEnvId(Long envId);

    void baseUpdateWithInstanceId(Long instanceId);
}
