package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.gitlab.BranchE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabCommitE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabJobE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabPipelineE;

/**
 * Created by Zenger on 2018/4/9.
 */
public interface GitlabProjectRepository {

    List<GitlabPipelineE> listPipeline(Integer projectId);

    List<GitlabPipelineE> listPipelines(Integer projectId, Integer page, Integer size);

    GitlabPipelineE getPipeline(Integer projectId, Integer pipelineId, String userName);

    GitlabCommitE getCommit(Integer projectId, String sha, String userName);

    List<GitlabJobE> listJobs(Integer projectId, Integer pipelineId, String userName);

    Boolean retry(Integer projectId, Integer pipelineId, String userName);

    Boolean cancel(Integer projectId, Integer pipelineId, String userName);

    List<BranchE> listBranches(Integer projectId);
}
