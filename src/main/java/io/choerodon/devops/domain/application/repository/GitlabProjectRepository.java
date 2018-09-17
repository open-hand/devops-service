package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.devops.domain.application.entity.gitlab.BranchE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabCommitE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabJobE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabPipelineE;
import io.choerodon.devops.infra.dataobject.gitlab.CommitStatuseDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Created by Zenger on 2018/4/9.
 */
public interface GitlabProjectRepository {

    List<GitlabPipelineE> listPipeline(Integer projectId, Integer userId);

    List<GitlabPipelineE> listPipelines(Integer projectId, Integer page, Integer size, Integer userId);

    GitlabPipelineE getPipeline(Integer projectId, Integer pipelineId, Integer userId);

    GitlabCommitE getCommit(Integer projectId, String sha, Integer userId);

    List<GitlabJobE> listJobs(Integer projectId, Integer pipelineId, Integer userId);

    Boolean retry(Integer projectId, Integer pipelineId, Integer userId);

    Boolean cancel(Integer projectId, Integer pipelineId, Integer userId);

    List<BranchE> listBranches(Integer projectId, Integer userId);

    List<CommitStatuseDO> getCommitStatuse(Integer projectId, String sha, Integer useId);

    void initMockService(GitlabServiceClient gitlabServiceClient);
}
