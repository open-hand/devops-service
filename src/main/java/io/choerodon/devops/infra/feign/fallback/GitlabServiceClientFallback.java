package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.event.GitlabUserEvent;
import io.choerodon.devops.infra.dataobject.gitlab.*;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabServiceClientFallback implements GitlabServiceClient {

    @Override
    public ResponseEntity<UserDO> queryUserByUsername(String userName) {
        return new ResponseEntity("error.user.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MemberDO> getUserMemberByUserId(Integer groupId, Integer userId) {
        return new ResponseEntity("error.group.member.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity deleteMember(Integer groupId, Integer userId) {
        return new ResponseEntity("error.remove.member", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MemberDO> insertMember(Integer groupId, RequestMemberDO requestMember) {
        return new ResponseEntity("error.add.member", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MemberDO> updateMember(Integer groupId, RequestMemberDO requestMember) {
        return new ResponseEntity("error.update.member", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<UserDO> createGitLabUser(String password, Integer projectsLimit, GitlabUserEvent gitlabUserEvent) {
        return new ResponseEntity("error.GitlabUser.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<UserDO> updateGitLabUser(String username, Integer projectsLimit, GitlabUserEvent gitlabUserEvent) {
        return new ResponseEntity("error.GitlabUser.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity deleteGitLabUser(String username) {
        return new ResponseEntity("error.GitlabUser.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GitlabProjectDO> createProject(Integer groupId, String projectName, String userName) {
        return new ResponseEntity("error.create.project", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GitlabProjectDO> updateProject(Integer gitlabProjectId, String projectCode) {
        return new ResponseEntity("error.project.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Map<String, Object>> addVariable(Integer gitlabProjectId, String key, String
            value, Boolean protecteds) {
        return new ResponseEntity("error.add.variable", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Map<String, Object>> addVariable(Integer projectId, String key, String value, Boolean protecteds, String userName) {
        return new ResponseEntity("error.variable.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity deleteProject(Integer gitlabProjectId, String userName) {
        return new ResponseEntity("error.service.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<PipelineDO>> listPipeline(Integer projectId) {
        return new ResponseEntity("error.pipeline.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<PipelineDO>> listPipelines(Integer projectId, Integer page, Integer size) {
        return new ResponseEntity("error.pipeline.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<PipelineDO> getPipeline(Integer projectId, Integer pipelineId, String userName) {
        return new ResponseEntity("error.pipeline.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<JobDO>> listJobs(Integer projectId, Integer pipelineId, String userName) {
        return new ResponseEntity("error.job.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity updateMergeRequest(Integer projectId, Integer merRequestId, String username) {
        return new ResponseEntity("error.mr.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MergeRequestDO> getMergeRequest(Integer projectId, Integer mergeRequestId, String username) {
        return new ResponseEntity("error.mr.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CommitDO>> listCommits(Integer projectId, Integer mergeRequestId) {
        return new ResponseEntity("error.mr.commits.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<BranchDO>> listBranches(Integer projectId) {
        return new ResponseEntity("error.select.branch", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<JobDO> getJob(Integer projectId, Integer jobId) {
        return new ResponseEntity("error.job.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<io.choerodon.devops.infra.dataobject.gitlab.CommitDO> getCommit(Integer projectId, String sha, String userName) {
        return new ResponseEntity("error.commit.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Map<String, Object>> insertProtectedBranches(Integer projectId, String name, String
            mergeAccessLevel, String pushAccessLevel, String userName) {
        return new ResponseEntity("error.projects.protected.branches.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ImpersonationTokenDO>> selectAll(String username) {
        return new ResponseEntity("error.access_token.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GroupDO> createGroup(GroupDO group, String userName) {
        return null;
    }

    @Override
    public ResponseEntity<GroupDO> createGroup(GroupDO groupDO) {
        return new ResponseEntity("error.group.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<GitlabProjectDO>> listProjects(Integer groupId, String userName) {
        return new ResponseEntity("error.group.listProjects", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ImpersonationTokenDO> createToken(String username) {
        return null;
    }

    @Override
    public ResponseEntity<List<ImpersonationTokenDO>> listTokenByUserName(String username) {
        return null;
    }

    @Override
    public ResponseEntity<ProjectHookDO> addHookByProjectHook(Integer projectId, ProjectHookDO projectHook, String userName) {
        return new ResponseEntity("error.hook.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GroupDO> queryGroupByName(String groupName) {
        return new ResponseEntity("error.group.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Boolean> createFile(Integer projectId, String userName) {
        return new ResponseEntity("error.file.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ImpersonationTokenDO> create(String username) {
        return new ResponseEntity("error.access_token.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Map<String, Object>> createProtectedBranches(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel, String userName) {
        return new ResponseEntity("error.branch.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<PipelineDO> retry(Integer projectId, Integer pipelineId, String userName) {
        return new ResponseEntity("error.pipeline.retry", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<PipelineDO> cancel(Integer projectId, Integer pipelineId, String userName) {
        return new ResponseEntity("error.pipeline.cancel", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MergeRequestDO> createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, String username) {
        return new ResponseEntity("error.mergeRequest.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MergeRequestDO> acceptMergeRequest(Integer projectId, Integer mergeRequestId, String mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, String username) {
        return new ResponseEntity("error.mergeRequext.accept", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TagDO>> getTags(Integer projectId, String username) {
        return new ResponseEntity("error.tags.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<TagDO> createTag(Integer projectId, String name, String ref, String username) {
        return new ResponseEntity("error.tags.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity deleteMergeRequest(Integer projectId, Integer mergeRequestId) {
        return new ResponseEntity("error.mergeRequest.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Object> deleteBranch(Integer projectId, String branchName, String username) {
        return new ResponseEntity("error.branch.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<BranchDO> createBranch(Integer projectId, String name, String source) {
        return new ResponseEntity("error.branch.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TagDO>> getPageTags(Integer projectId, int page, int perPage) {
        return new ResponseEntity("error.pageTags.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity enabledUserByUsername(String username) {
        return new ResponseEntity("error.user.unblockUser", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity disEnabledUserByUsername(String username) {
        return new ResponseEntity("error.user.blockUser", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
