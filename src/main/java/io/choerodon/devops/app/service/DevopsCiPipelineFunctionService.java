package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCiPipelineFunctionDTO;

public interface DevopsCiPipelineFunctionService {

    void deleteByPipelineId(Long pipelineId);

    void baseCreate(DevopsCiPipelineFunctionDTO devopsCiPipelineFunctionDTO);

    List<DevopsCiPipelineFunctionDTO> listFunctionsByDevopsPipelineId(Long pipelineId);


}
