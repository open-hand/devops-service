package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiPipelineMavenDTO;

/**
 * @author scp
 * @date 2020/7/22
 * @description
 */
public interface CiPipelineMavenService {

    void createOrUpdate(CiPipelineMavenDTO ciPipelineMavenDTO);

    CiPipelineMavenDTO queryByGitlabPipelineId(Long gitlabPipelineId, String jobName);
}
