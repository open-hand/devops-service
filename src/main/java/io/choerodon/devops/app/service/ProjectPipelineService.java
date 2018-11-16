package io.choerodon.devops.app.service;

/**
 * Created by Zenger on 2018/4/10.
 */
public interface ProjectPipelineService {

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
