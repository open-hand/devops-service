package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineAppServiceDeployDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:04 2019/7/15
 * Description:
 */
public interface PipelineAppDeployService {
    PipelineAppServiceDeployDTO baseCreate(PipelineAppServiceDeployDTO pipelineAppServiceDeployDTO);

    PipelineAppServiceDeployDTO baseUpdate(PipelineAppServiceDeployDTO pipelineAppServiceDeployDTO);

    void baseDeleteById(Long appDelpoyId);

    PipelineAppServiceDeployDTO baseQueryById(Long appDelpoyId);

    List<PipelineAppServiceDeployDTO> baseQueryByAppId(Long appServiceId);

    void baseCheckName(String name, Long envId);

    List<PipelineAppServiceDeployDTO> baseQueryByValueId(Long valueId);

    List<PipelineAppServiceDeployDTO> baseQueryByEnvId(Long envId);

    void baseUpdateWithInstanceId(Long instanceId);
}
