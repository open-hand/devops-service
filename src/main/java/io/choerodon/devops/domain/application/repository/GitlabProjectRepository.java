package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.gitlab.*;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO;
import io.choerodon.devops.infra.dataobject.gitlab.CommitStatuseDO;

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

    List<CommitDO> listCommits(Integer projectId, Integer userId, Integer page, Integer size);

    GitlabMemberE getProjectMember(Integer projectId, Integer userId);

    void deleteBranch(Integer projectId, String branchName, Integer userId);

    List<GitlabMemberE> getAllMemberByProjectId(Integer projectId);
}
