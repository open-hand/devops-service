package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CiCdStageVO;
import io.choerodon.devops.infra.dto.DevopsCdStageDTO;

public interface DevopsCdStageService {
    /**
     * 创建流水线stage
     * @param devopsCdStageDTO
     * @return
     */
    DevopsCdStageDTO create(DevopsCdStageDTO devopsCdStageDTO);

    List<DevopsCdStageDTO> listByPipelineId(Long ciCdPipelineId);

    void deleteById(Long stageId);

    void update(CiCdStageVO ciCdStageVO);

    void deleteByPipelineId(Long ciCdPipelineId);

    /**
     * 根据流水线id，查询cd流水线阶段列表
     * @param pipelineId 流水线id
     * @return
     */
    List<DevopsCdStageDTO> queryByPipelineId(Long pipelineId);
}
