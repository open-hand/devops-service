package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import feign.FeignException;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabCommitE;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabJobE;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabPipelineE;
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDTO;
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
        ResponseEntity<List<PipelineDO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listPipeline(projectId, userId);
        } catch (FeignException e) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(responseEntity.getBody(), GitlabPipelineE.class);
    }

    @Override
    public List<GitlabPipelineE> listPipelines(Integer projectId, Integer page, Integer size, Integer userId) {
        ResponseEntity<List<PipelineDO>> responseEntity;
        try {
            responseEntity =
                    gitlabServiceClient.pagePipeline(projectId, page, size, userId);
        } catch (FeignException e) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(responseEntity.getBody(), GitlabPipelineE.class);
    }

    @Override
    public GitlabPipelineE getPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<PipelineDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryPipeline(projectId, pipelineId, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabPipelineE.class);
    }

    @Override
    public GitlabCommitE getCommit(Integer projectId, String sha, Integer userId) {
        ResponseEntity<CommitDTO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryCommit(projectId, sha, userId);
        } catch (FeignException e) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabCommitE.class);
    }

    @Override
    public List<GitlabJobE> listJobs(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<List<JobDO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listJobs(projectId, pipelineId, userId);
        } catch (FeignException e) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(responseEntity.getBody(), GitlabJobE.class);
    }

    @Override
    public Boolean retry(Integer projectId, Integer pipelineId, Integer userId) {
        try {
            gitlabServiceClient.retry(projectId, pipelineId, userId);
        } catch (FeignException e) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean cancel(Integer projectId, Integer pipelineId, Integer userId) {
        try {
            gitlabServiceClient.cancelPipeline(projectId, pipelineId, userId);
        } catch (FeignException e) {
            return false;
        }
        return true;
    }

    @Override
    public List<CommitStatuseDO> getCommitStatus(Integer projectId, String sha, Integer useId) {
        ResponseEntity<List<CommitStatuseDO>> commitStatuse;
        try {
            commitStatuse = gitlabServiceClient.listCommitStatus(projectId, sha, useId);
        } catch (FeignException e) {
            return Collections.emptyList();
        }
        return commitStatuse.getBody();
    }

    @Override
    public List<CommitDTO> listCommits(Integer projectId, Integer userId, Integer page, Integer size) {
        try {
            List<CommitDTO> commitDOS = new LinkedList<>();
            commitDOS.addAll(gitlabServiceClient.listCommits(projectId, page, size, userId).getBody());
            return commitDOS;
        } catch (FeignException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public GitlabMemberE getProjectMember(Integer projectId, Integer userId) {
        try {
            return ConvertHelper.convert(gitlabServiceClient.getProjectMember(
                    projectId, userId).getBody(), GitlabMemberE.class);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public void deleteBranch(Integer projectId, String branchName, Integer userId) {
        try {
            gitlabServiceClient.deleteBranch(projectId, branchName, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<GitlabMemberE> getAllMemberByProjectId(Integer projectId) {
        try {
            return ConvertHelper
                    .convertList(gitlabServiceClient.listMemberByProject(projectId).getBody(), GitlabMemberE.class);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

}
