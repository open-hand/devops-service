package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsCiStageVO;
import io.choerodon.devops.infra.dto.DevopsCiStageDTO;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:23
 */
public interface DevopsCiStageService {

    /**
     * 创建ci流水线stage
     * @param devopsCiStageDTO
     * @return
     */
    DevopsCiStageDTO create(DevopsCiStageDTO devopsCiStageDTO);

    /**
     * 查询流水线里的阶段
     * @param ciPipelineId
     * @return
     */
    List<DevopsCiStageDTO> listByPipelineId(Long ciPipelineId);

    /**
     * 删除阶段
     * @param id
     */
    void deleteById(Long id);

    void update(DevopsCiStageVO devopsCiStageVO);

    void deleteByPipelineId(Long ciPipelineId);
}
