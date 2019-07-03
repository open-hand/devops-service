package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabCommitE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabJobE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabPipelineE;
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO;
import io.choerodon.devops.infra.dataobject.gitlab.CommitStatuseDO;
import io.choerodon.devops.infra.dataobject.gitlab.JobDO;
import io.choerodon.devops.infra.dataobject.gitlab.PipelineDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/4/9.
 */
@Component
public class GitlabProjectRepositoryImpl implements GitlabProjectRepository {

    private GitlabServiceClient gitlabServiceClient;

    public GitlabProjectRepositoryImpl(GitlabServiceClient gitlabServiceClient) {
        this.gitlabServiceClient = gitlabServiceClient;
    }

    @Override
    public List<GitlabPipelineE> listPipeline(Integer projectId, Integer userId) {
        ResponseEntity<List<PipelineDO>> responseEntity = gitlabServiceClient.listPipeline(projectId, userId);
        if (responseEntity.getStatusCodeValue() == 500) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(responseEntity.getBody(), GitlabPipelineE.class);
    }

    @Override
    public List<GitlabPipelineE> listPipelines(Integer projectId, Integer page, Integer size, Integer userId) {
        ResponseEntity<List<PipelineDO>> responseEntity =
                gitlabServiceClient.listPipelines(projectId, page, size, userId);
        if (responseEntity.getStatusCodeValue() == 500) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(responseEntity.getBody(), GitlabPipelineE.class);
    }

    @Override
    public GitlabPipelineE getPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<PipelineDO> responseEntity = gitlabServiceClient.getPipeline(projectId, pipelineId, userId);
        return ConvertHelper.convert(responseEntity.getBody(), GitlabPipelineE.class);
    }

    @Override
    public GitlabCommitE getCommit(Integer projectId, String sha, Integer userId) {

        ResponseEntity<CommitDO> responseEntity = gitlabServiceClient.getCommit(projectId, sha, userId);
        if (responseEntity.getStatusCodeValue() == 500) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabCommitE.class);
    }

    @Override
    public List<GitlabJobE> listJobs(Integer projectId, Integer pipelineId, Integer userId) {

        ResponseEntity<List<JobDO>> responseEntity = gitlabServiceClient.listJobs(projectId, pipelineId, userId);
        if (responseEntity.getStatusCodeValue() == 500) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(responseEntity.getBody(), GitlabJobE.class);
    }

    @Override
    public Boolean retry(Integer projectId, Integer pipelineId, Integer userId) {

        ResponseEntity responseEntity = gitlabServiceClient.retry(projectId, pipelineId, userId);
        return responseEntity.getStatusCodeValue() != 500;
    }

    @Override
    public Boolean cancel(Integer projectId, Integer pipelineId, Integer userId) {

        ResponseEntity responseEntity = gitlabServiceClient.cancel(projectId, pipelineId, userId);
        return responseEntity.getStatusCodeValue() != 500;
    }

    @Override
    public List<CommitStatuseDO> getCommitStatus(Integer projectId, String sha, Integer useId) {
        ResponseEntity<List<CommitStatuseDO>> commitStatus = gitlabServiceClient.getCommitStatus(projectId, sha, useId);
        if (commitStatus.getStatusCodeValue() == 500) {
            return new ArrayList<>();
        }
        return commitStatus.getBody();
    }

    @Override
    public List<CommitDO> listCommits(Integer projectId, Integer userId, Integer page, Integer size) {
        List<CommitDO> commitDOS = new LinkedList<>();
        commitDOS.addAll(gitlabServiceClient.listCommits(projectId, page, size, userId).getBody());
        return commitDOS;
    }

    @Override
    public GitlabMemberE getProjectMember(Integer projectId, Integer userId) {
            return ConvertHelper.convert(gitlabServiceClient.getProjectMember(
                    projectId, userId).getBody(), GitlabMemberE.class);
    }

    @Override
    public void deleteBranch(Integer projectId, String branchName, Integer userId) {
        gitlabServiceClient.deleteBranch(projectId, branchName, userId);
    }

    @Override
    public List<GitlabMemberE> getAllMemberByProjectId(Integer projectId) {
            return ConvertHelper
                    .convertList(gitlabServiceClient.getAllMemberByProjectId(projectId).getBody(), GitlabMemberE.class);

    }

}
