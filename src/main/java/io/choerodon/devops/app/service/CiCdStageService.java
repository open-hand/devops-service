package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CiCdStageVO;
import io.choerodon.devops.infra.dto.CiCdStageDTO;

public interface CiCdStageService {
    /**
     * 创建流水线stage
     * @param ciCdStageDTO
     * @return
     */
    CiCdStageDTO create(CiCdStageDTO ciCdStageDTO);

    List<CiCdStageDTO> listByPipelineId(Long ciCdPipelineId);

    void deleteById(Long stageId);

    void update(CiCdStageVO ciCdStageVO);
}
