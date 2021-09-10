package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.enums.JobTypeEnum;

public interface DevopsCdJobService {
    DevopsCdJobDTO create(DevopsCdJobDTO devopsCdJobDTO);

    /**
     * 根据流水线id,查询job列表
     * @param pipelineId
     * @return
     */
    List<DevopsCdJobDTO> listByPipelineId(Long pipelineId);

    /**
     * 根据流水线id,查询job列表
     * @param jobTypeEnum
     * @return
     */
    List<DevopsCdJobDTO> listByType(JobTypeEnum jobTypeEnum);


    void deleteByStageId(Long stageId);

    void deleteByPipelineId(Long pipelineId);

    DevopsCdJobDTO queryById(Long stageId);

    void baseUpdate(DevopsCdJobDTO devopsCdJobDTO);

    void baseDelete(Long id);
}
