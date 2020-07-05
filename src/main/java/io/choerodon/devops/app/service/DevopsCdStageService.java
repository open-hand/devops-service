package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCdStageDTO;

public interface DevopsCdStageService {

    /**
     * 根据流水线id，查询cd流水线阶段列表
     * @param pipelineId 流水线id
     * @return
     */
    List<DevopsCdStageDTO> queryByPipelineId(Long pipelineId);

    DevopsCdStageDTO create(DevopsCdStageDTO devopsCdStageDTO);

    void deleteById(Long stageId);

    void deleteByPipelineId(Long pipelineId);
}
