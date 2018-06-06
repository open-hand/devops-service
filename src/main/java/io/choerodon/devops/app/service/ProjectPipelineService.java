package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.ProjectPipelineResultTotalDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/10.
 */
public interface ProjectPipelineService {

    /**
     * 查询应用下的Pipeline信息
     *
     * @param projectId   项目id
     * @param appId       应用Id
     * @param pageRequest 分页参数
     * @return ProjectPipelineResultTotalDTO
     */
    ProjectPipelineResultTotalDTO listPipelines(Long projectId, Long appId, PageRequest pageRequest);

    /**
     * Retry jobs in a pipeline
     *
     * @param gitlabProjectId gitlab项目id
     * @param pipelineId      流水线id
     * @return Boolean
     */
    Boolean retry(Long gitlabProjectId, Long pipelineId);

    /**
     * Cancel jobs in a pipeline
     *
     * @param gitlabProjectId gitlab项目id
     * @param pipelineId      流水线id
     * @return Boolean
     */
    Boolean cancel(Long gitlabProjectId, Long pipelineId);
}
