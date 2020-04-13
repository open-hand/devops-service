package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.SonarQubeConfigVO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;

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
     *
     * @param devopsCiJobDTO
     * @return
     */
    DevopsCiJobDTO create(DevopsCiJobDTO devopsCiJobDTO);

    /**
     * 删除stage下的job
     *
     * @param stageId
     */
    void deleteByStageId(Long stageId);

    /**
     * 查询pipeline下的jobs
     *
     * @param ciPipelineId
     * @return
     */
    List<DevopsCiJobDTO> listByPipelineId(Long ciPipelineId);

    /**
     * sonar的连接测试
     *
     * @param projectId
     * @param sonarQubeConfigVO
     * @return
     */
    Boolean sonarConnect(Long projectId, SonarQubeConfigVO sonarQubeConfigVO);

    /**
     * 查询job日志
     * @param gitlabProjectId
     * @param jobId gitlab job id
     * @return
     */
    String queryTrace(Long gitlabProjectId, Long jobId);

    /**
     * 重试job
     * @param gitlabProjectId
     * @param jobId gitlab job id
     * @return
     */
    JobDTO retryJob(Long gitlabProjectId, Long jobId);

    /**
     * 删除流水线下的job
     * @param ciPipelineId
     */
    void deleteByPipelineId(Long ciPipelineId);
}
