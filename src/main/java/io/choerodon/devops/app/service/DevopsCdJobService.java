package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCdJobDTO;

public interface DevopsCdJobService {
    DevopsCdJobDTO create(DevopsCdJobDTO devopsCdJobDTO);

    /**
     * 根据流水线id,查询job列表
     * @param pipelineId
     * @return
     */
    List<DevopsCdJobDTO> listByPipelineId(Long pipelineId);


    void deleteByStageId(Long stageId);

    void deleteByPipelineId(Long pipelineId);

    String queryTrace(Long gitlabProjectId, Long jobId);

    void retryJob(Long projectId, Long gitlabProjectId, Long jobId);

    DevopsCdJobDTO queryById(Long stageId);
}
