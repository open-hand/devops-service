package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.dto.RepositoryFileDTO;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabServiceClientFallback implements GitlabServiceClient {

    @Override
    public ResponseEntity<GitLabUserDTO> queryUserById(Integer userId) {
        throw new CommonException("error.user.get");
    }

    @Override
    public ResponseEntity<GitLabUserDTO> queryUserByUserName(String username) {
        return new ResponseEntity("error.user.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MemberDTO> queryGroupMember(Integer groupId, Integer userId) {
        throw new CommonException("error.group.member.get");
    }

    @Override
    public ResponseEntity deleteMember(Integer groupId, Integer userId) {
        throw new CommonException("error.remove.member");
    }

    @Override
    public ResponseEntity<MemberDTO> createGroupMember(Integer groupId, MemberDTO requestMember) {
        throw new CommonException("error.add.member");
    }

    @Override
    public ResponseEntity<MemberDTO> updateGroupMember(Integer groupId, MemberDTO requestMember) {
        throw new CommonException("error.update.member");
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> updateProject(Integer projectId, Integer userId) {
        throw new CommonException("error.project.update");
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        throw new CommonException("error.project.create");
    }

    @Override
    public ResponseEntity createDeploykey(Integer projectId, String title, String key, boolean canPush, Integer userId) {
        throw new CommonException("error.deploykey.create");
    }

    @Override
    public ResponseEntity<List<DeployKeyDTO>> listDeploykey(Integer projectId, Integer userId) {
        throw new CommonException("error.deploykey.get");
    }

    @Override
    public ResponseEntity<Map<String, Object>> addProjectVariable(Integer projectId, String key, String value, Boolean protecteds, Integer userId) {
        throw new CommonException("error.variable.get");
    }

    @Override
    public ResponseEntity deleteProjectById(Integer gitlabProjectId, Integer userId) {
        throw new CommonException("error.service.delete");
    }

    @Override
    public ResponseEntity deleteProjectByName(String groupName, String projectName, Integer userId) {
        throw new CommonException("error.service.delete");
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> queryProjectById(Integer projectId) {
        throw new CommonException("error.project.get");
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> queryProjectByName(Integer userId, String groupName, String projectName) {
        throw new CommonException("error.project.get");
    }

    @Override
    public ResponseEntity<List<VariableDTO>> listVariable(Integer projectId, Integer userId) {
        throw new CommonException("error.variable.get");
    }

    @Override
    public ResponseEntity<List<GitlabPipelineDTO>> listPipeline(Integer projectId, Integer userId) {
        return new ResponseEntity("error.pipeline.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<GitlabPipelineDTO>> pagePipeline(Integer projectId, Integer page, Integer size, Integer userId) {
        return new ResponseEntity("error.pipelines.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<GitlabPipelineDTO> queryPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        throw new CommonException("error.pipelines.select");
    }

    @Override
    public ResponseEntity<List<JobDTO>> listJobs(Integer projectId, Integer pipelineId, Integer userId) {
        return new ResponseEntity("error.job.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity updateMergeRequest(Integer projectId, Integer merRequestId, Integer userId) {
        throw new CommonException("error.mr.update");
    }

    @Override
    public ResponseEntity<MergeRequestDTO> getMergeRequest(Integer projectId, Integer mergeRequestId, Integer userId) {
        throw new CommonException("error.mr.get");
    }

    @Override
    public ResponseEntity<List<CommitDTO>> listCommits(Integer projectId, Integer mergeRequestId, Integer userId) {
        return new ResponseEntity("error.mr.commits.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<MergeRequestDTO>> getMergeRequestList(Integer projectId) {
        throw new CommonException("error.mergerequest.get");
    }

    @Override
    public ResponseEntity<List<BranchDTO>> listBranch(Integer projectId, Integer userId) {
        throw new CommonException("error.select.branch");
    }

    @Override
    public ResponseEntity<CommitDTO> queryCommit(Integer projectId, String sha, Integer userId) {

        return new ResponseEntity("error.commit.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CommitDTO>> listCommits(Integer projectId, Integer page, Integer size, Integer
            userId) {
        throw new CommonException("error.commit.select");
    }

    @Override
    public ResponseEntity<List<CommitDTO>> getCommits(Integer projectId, String branchName, String since) {
        throw new CommonException("error.commits.get");
    }

    @Override
    public ResponseEntity<GroupDTO> createGroup(GroupDTO groupDO, Integer userId) {
        throw new CommonException("error.group.create");
    }

    @Override
    public ResponseEntity<List<GitlabProjectDTO>> listProjects(Integer groupId, Integer userId) {
        throw new CommonException("error.group.listProjects");
    }

    @Override
    public ResponseEntity<GroupDTO> queryGroupByName(String groupName, Integer userId) {
        throw new CommonException("error.group.get");
    }

    @Override
    public ResponseEntity<RepositoryFileDTO> createFile(Integer projectId, String path, String content, String
            commitMessage, Integer userId) {
        throw new CommonException("error.file.create");
    }

    @Override
    public ResponseEntity<RepositoryFileDTO> createFile(Integer projectId, String path, String content, String
            commitMessage, Integer userId, String branchName) {
        throw new CommonException("error.file.create");
    }

    @Override
    public ResponseEntity<RepositoryFileDTO> updateFile(Integer projectId, String path, String content, String
            commitMessage, Integer userId) {
        throw new CommonException("error.file.update");
    }

    @Override
    public ResponseEntity deleteFile(Integer projectId, String path, String commitMessage, Integer userId) {
        throw new CommonException("error.file.delete");
    }

    @Override
    public ResponseEntity<RepositoryFileDTO> getFile(Integer projectId, String commit, String filePath) {
        return new ResponseEntity("error.file.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<ImpersonationTokenDO> create(Integer userId) {
        throw new CommonException("error.access_token.create");
    }


    @Override
    public ResponseEntity<MergeRequestDTO> createMergeRequest(Integer projectId, String sourceBranch, String
            targetBranch, String title, String description, Integer userId) {
        throw new CommonException("error.mergeRequest.create");
    }

    @Override
    public ResponseEntity<MergeRequestDTO> acceptMergeRequest(Integer projectId, Integer mergeRequestId, String
            mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, Integer userId) {
        throw new CommonException("error.mergeRequext.accept");
    }

    @Override
    public ResponseEntity<List<TagDTO>> getTags(Integer projectId, Integer userId) {
        throw new CommonException("error.tags.get");
    }

    @Override
    public ResponseEntity<TagDTO> createTag(Integer projectId, String name, String ref,
                                            String msg, String releaseNotes, Integer userId) {
        throw new CommonException("error.tags.create");
    }

    @Override
    public ResponseEntity deleteTag(Integer projectId, String name, Integer userId) {
        throw new CommonException("error.tag.delete");
    }

    @Override
    public ResponseEntity deleteMergeRequest(Integer projectId, Integer mergeRequestId) {
        throw new CommonException("error.mergeRequest.delete");
    }

    @Override
    public ResponseEntity<Object> deleteBranch(Integer projectId, String branchName, Integer userId) {
        throw new CommonException("error.branch.delete");
    }

    @Override
    public ResponseEntity<BranchDTO> queryBranch(Integer projectId, String branchName) {
        throw new CommonException("error.branch.get");
    }

    @Override
    public ResponseEntity<BranchDTO> createBranch(Integer projectId, String name, String source, Integer userId) {
        throw new CommonException("error.branch.create");
    }

    @Override
    public ResponseEntity<List<TagDTO>> getPageTags(Integer projectId, int page, int perPage, Integer userId) {
        throw new CommonException("error.pageTag.get");
    }

    @Override
    public ResponseEntity<ProjectHookDTO> createProjectHook(Integer projectId, Integer userId, ProjectHookDTO
            projectHook) {
        throw new CommonException("error.projecthook.create");
    }

    @Override
    public ResponseEntity<ProjectHookDTO> updateProjectHook(Integer projectId, Integer hookId, Integer userId) {
        throw new CommonException("error.projecthook.update");
    }

    @Override
    public ResponseEntity updateGroup(Integer groupId, Integer userId, GroupDTO group) {
        throw new CommonException("error.group.update");
    }

    @Override
    public ResponseEntity<GitLabUserDTO> createUser(String password, Integer projectsLimit, GitlabUserReqDTO userReqDTO) {
        throw new CommonException("error.gitlab.user.create");
    }

    @Override
    public ResponseEntity<GitLabUserDTO> updateGitLabUser(Integer userId, Integer projectsLimit, GitlabUserReqDTO userReqDTO) {
        throw new CommonException("error.gitlab.user.update");
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> batchAddProjectVariable(Integer projectId, Integer userId, @Valid List<VariableDTO> variableDTODTOS) {
        throw new CommonException("error.variable.create");
    }

    @Override
    public ResponseEntity<ImpersonationTokenDO> createProjectToken(Integer userId) {
        throw new CommonException("error.project.token.create");
    }

    @Override
    public ResponseEntity<List<ImpersonationTokenDO>> listProjectToken(Integer userId) {
        throw new CommonException("error.project.token.list");
    }

    @Override
    public ResponseEntity<CompareResultDTO> queryCompareResult(Integer projectId, String from, String to) {
        throw new CommonException("error.compare.query");
    }

    @Override
    public ResponseEntity<Map<String, Object>> createProtectedBranch(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel, Integer userId) {
        throw new CommonException("error.compare.query");
    }

    @Override
    public ResponseEntity<List<CommitStatusDTO>> listCommitStatus(Integer projectId, String sha, Integer userId) {
        throw new CommonException("error.compare.list");
    }

    @Override
    public ResponseEntity<GitlabPipelineDTO> retryPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        throw new CommonException("error.pipeline.retry");
    }

    @Override
    public ResponseEntity<GitlabPipelineDTO> cancelPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        throw new CommonException("error.pipeline.cancel");
    }

    @Override
    public ResponseEntity<TagDTO> updateTag(Integer projectId, String name, String releaseNotes, Integer userId) {
        throw new CommonException("error.tag.update");
    }

    @Override
    public ResponseEntity enableUser(Integer userId) {
        throw new CommonException("error.gitlab.user.enable");
    }

    @Override
    public ResponseEntity disableUser(Integer userId) {
        throw new CommonException("error.gitlab.user.disable");
    }

    @Override
    public ResponseEntity<List<ProjectHookDTO>> listProjectHook(Integer projectId, Integer userId) {
        throw new CommonException("error.gitlab.query");
    }

    @Override
    public ResponseEntity createProjectMember(Integer projectId, MemberDTO memberDTO) {
        return null;
    }

    @Override
    public ResponseEntity updateProjectMember(Integer projectId, List<MemberDTO> list) {
        return null;
    }

    @Override
    public ResponseEntity<MemberDTO> getProjectMember(Integer projectId, Integer userId) {
        return null;
    }

    @Override
    public ResponseEntity deleteProjectMember(Integer projectId, Integer userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<MemberDTO>> listMemberByProject(Integer projectId) {
        return null;
    }

    @Override
    public ResponseEntity<List<GitlabProjectDTO>> listProjectByUser(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<Boolean> checkEmail(String email) {
        return null;
    }
}
