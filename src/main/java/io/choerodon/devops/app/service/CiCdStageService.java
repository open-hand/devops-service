package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsCdStageVO;
import io.choerodon.devops.infra.dto.DevopsCdStageDTO;

public interface CiCdStageService {
    /**
     * 创建流水线stage
     * @param devopsCdStageDTO
     * @return
     */
    DevopsCdStageDTO create(DevopsCdStageDTO devopsCdStageDTO);

    List<DevopsCdStageDTO> listByPipelineId(Long ciCdPipelineId);

    void deleteById(Long stageId);

    void update(DevopsCdStageVO devopsCdStageVO);

    void deleteByPipelineId(Long ciCdPipelineId);
}
