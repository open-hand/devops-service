package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCiPipelineVariableDTO;

/**
 * 流水线配置的CI变量(DevopsCiPipelineVariable)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-03 16:18:15
 */
public interface DevopsCiPipelineVariableService {

    void baseCreate(DevopsCiPipelineVariableDTO devopsCiPipelineVariableDTO);

    void deleteByPipelineId(Long pipelineId);

    List<DevopsCiPipelineVariableDTO> listByPipelineId(Long pipelineId);
}

