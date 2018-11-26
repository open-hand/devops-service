package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.dto.gitlab.MemberDTO;
import io.choerodon.devops.domain.application.entity.gitlab.CompareResultsE;
import io.choerodon.devops.domain.application.event.GitlabUserEvent;
import io.choerodon.devops.domain.application.valueobject.DeployKey;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.RepositoryFile;
import io.choerodon.devops.domain.application.valueobject.Variable;
import io.choerodon.devops.infra.dataobject.gitlab.*;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabServiceClientFallback implements GitlabServiceClient {

    @Override
    public ResponseEntity<UserDO> queryUserByUserId(Integer userId) {
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
    public ResponseEntity<UserDO> updateGitLabUser(Integer userId, Integer projectsLimit, GitlabUserEvent gitlabUserEvent) {
        return new ResponseEntity("error.GitlabUser.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GitlabProjectDO> updateProject(Integer projectId, Integer userId) {
        return new ResponseEntity("error.project.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GitlabProjectDO> createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        return new ResponseEntity("error.project.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity createDeploykey(Integer projectId, String title, String key, boolean canPush, Integer userId) {
        return new ResponseEntity("error.deploykey.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<DeployKey>> getDeploykeys(Integer projectId, Integer userId) {
        return new ResponseEntity("error.deploykey.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Map<String, Object>> addVariable(Integer projectId, String key, String value, Boolean protecteds, Integer userId) {
        return new ResponseEntity("error.variable.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity deleteProject(Integer gitlabProjectId, Integer userId) {
        return new ResponseEntity("error.service.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity deleteProjectByProjectName(String groupName, String projectName, Integer userId) {
        return new ResponseEntity("error.service.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GitlabProjectDO> getProjectById(Integer projectId) {
        return new ResponseEntity("error.project.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GitlabProjectDO> getProjectByName(Integer userId, String groupName, String projectName) {
        return new ResponseEntity("error.project.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Variable>> getVariable(Integer projectId, Integer userId) {
        return new ResponseEntity("error.variable.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<PipelineDO>> listPipeline(Integer projectId, Integer userId) {
        return new ResponseEntity("error.pipeline.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<PipelineDO>> listPipelines(Integer projectId, Integer page, Integer size, Integer userId) {
        return new ResponseEntity("error.pipelines.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<PipelineDO> getPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        return new ResponseEntity("error.pipelines.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<JobDO>> listJobs(Integer projectId, Integer pipelineId, Integer userId) {
        return new ResponseEntity("error.job.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity updateMergeRequest(Integer projectId, Integer merRequestId, Integer userId) {
        return new ResponseEntity("error.mr.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MergeRequestDO> getMergeRequest(Integer projectId, Integer mergeRequestId, Integer userId) {
        return new ResponseEntity("error.mr.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CommitDO>> listCommits(Integer projectId, Integer mergeRequestId, Integer userId) {
        return new ResponseEntity("error.mr.commits.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<MergeRequestDO>> getMergeRequestList(Integer projectId) {
        return new ResponseEntity("error.mergerequest.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<BranchDO>> listBranches(Integer projectId, Integer userId) {
        return new ResponseEntity("error.select.branch", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<io.choerodon.devops.infra.dataobject.gitlab.CommitDO> getCommit(Integer projectId, String sha, Integer userId) {
        return new ResponseEntity("error.commit.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CommitDO>> listCommits(Integer projectId, Integer page, Integer size, Integer userId) {
        return new ResponseEntity("error.commit.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CommitStatuseDO>> getCommitStatuse(Integer projectId, String sha, Integer userId) {
        return new ResponseEntity("error.commitStatuse.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<List<CommitDO>> getCommits(Integer projectId, String branchName, String since) {
        return new ResponseEntity("error.commits.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GroupDO> createGroup(GroupDO groupDO, Integer userId) {
        return new ResponseEntity("error.group.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<GitlabProjectDO>> listProjects(Integer groupId, Integer userId) {
        return new ResponseEntity("error.group.listProjects", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ImpersonationTokenDO> createToken(Integer userId) {
        return new ResponseEntity("error.token.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ImpersonationTokenDO>> listTokenByUserId(Integer userId) {
        return new ResponseEntity("error.token.query", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GroupDO> queryGroupByName(String groupName, Integer userId) {
        return new ResponseEntity("error.group.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<RepositoryFile> createFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        return new ResponseEntity("error.file.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<RepositoryFile> updateFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        return new ResponseEntity("error.file.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity deleteFile(Integer projectId, String path, String commitMessage, Integer userId) {
        return new ResponseEntity("error.file.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<RepositoryFile> getFile(Integer projectId, String commit, String filePath) {
        return new ResponseEntity("error.file.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<CompareResultsE> getCompareResults(Integer projectId, String from, String to) {
        return new ResponseEntity("error.diffs.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ImpersonationTokenDO> create(Integer userId) {
        return new ResponseEntity("error.access_token.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Map<String, Object>> createProtectedBranches(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel, Integer userId) {
        return new ResponseEntity("error.branch.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<PipelineDO> retry(Integer projectId, Integer pipelineId, Integer userId) {
        return new ResponseEntity("error.pipeline.retry", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<PipelineDO> cancel(Integer projectId, Integer pipelineId, Integer userId) {
        return new ResponseEntity("error.pipeline.cancel", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MergeRequestDO> createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, Integer userId) {
        return new ResponseEntity("error.mergeRequest.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MergeRequestDO> acceptMergeRequest(Integer projectId, Integer mergeRequestId, String mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, Integer userId) {
        return new ResponseEntity("error.mergeRequext.accept", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TagDO>> getTags(Integer projectId, Integer userId) {
        return new ResponseEntity("error.tags.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<TagDO> createTag(Integer projectId, String name, String ref,
                                           String msg, String releaseNotes, Integer userId) {
        return new ResponseEntity("error.tags.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<TagDO> updateTagRelease(Integer projectId, String name, String releaseNotes, Integer userId) {
        return new ResponseEntity("error.tags.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity deleteTag(Integer projectId, String name, Integer userId) {
        return new ResponseEntity("error.tag.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity deleteMergeRequest(Integer projectId, Integer mergeRequestId) {
        return new ResponseEntity("error.mergeRequest.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Object> deleteBranch(Integer projectId, String branchName, Integer userId) {
        return new ResponseEntity("error.branch.delete", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<BranchDO> getBranch(Integer projectId, String branchName) {
        return new ResponseEntity("error.branch.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<BranchDO> createBranch(Integer projectId, String name, String source, Integer userId) {
        return new ResponseEntity("error.branch.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<TagDO>> getPageTags(Integer projectId, int page, int perPage, Integer userId) {
        return new ResponseEntity("error.pageTags.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity enabledUserByUserId(Integer userId) {
        return new ResponseEntity("error.user.unblockUser", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity disEnabledUserByUserId(Integer userId) {
        return new ResponseEntity("error.user.blockUser", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProjectHook> createProjectHook(Integer projectId, Integer userId, ProjectHook projectHook) {
        return new ResponseEntity("error.projecthook.create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProjectHook> updateProjectHook(Integer projectId, Integer hookId, Integer userId) {
        return new ResponseEntity("error.projecthook.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProjectHook>> getProjectHook(Integer projectId, Integer userId) {
        return new ResponseEntity("error.projecthook.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity updateGroup(Integer groupId, Integer userId, GroupDO group) {
        return new ResponseEntity("error.group.update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity addMemberIntoProject(Integer projectId, MemberDTO memberDTO) {
        return new ResponseEntity("error.member.add", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity removeMemberFromProject(Integer projectId, Integer userId) {
        return new ResponseEntity("error.member.remove", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MemberDO> getProjectMember(Integer projectId, Integer userId) {
        return new ResponseEntity("error.project.member.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity getAllMemberByProjectId(Integer projectId) {
        return new ResponseEntity("error.project.member.list", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<GitlabProjectDO>> getProjectsByUserId(Integer userId) {
        return new ResponseEntity("error.project.get.by.userId", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
