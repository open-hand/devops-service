package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiJobDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:24
 */
public interface DevopsCiJobService {
    /**
     * 创建ci流水线job
     * @param devopsCiJobDTO
     * @return
     */
    DevopsCiJobDTO create(DevopsCiJobDTO devopsCiJobDTO);

    void deleteByStageId(Long stageId);
}
