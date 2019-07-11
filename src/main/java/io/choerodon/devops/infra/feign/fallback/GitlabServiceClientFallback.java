package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Map;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.gitlab.MemberVO;
import io.choerodon.devops.api.vo.gitlab.VariableDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.CompareResultsE;
import io.choerodon.devops.app.eventhandler.payload.GitlabUserPayload;
import io.choerodon.devops.domain.application.valueobject.DeployKey;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.RepositoryFile;
import io.choerodon.devops.domain.application.valueobject.Variable;
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
    public ResponseEntity<UserDO> queryUserByUserId(Integer userId) {
        throw new CommonException("error.user.get");
    }

    @Override
    public ResponseEntity<UserDO> queryUserByUserName(String username) {
        return new ResponseEntity("error.user.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<MemberDTO> getUserMemberByUserId(Integer groupId, Integer userId) {
        throw new CommonException("error.group.member.get");
    }

    @Override
    public ResponseEntity deleteMember(Integer groupId, Integer userId) {
        throw new CommonException("error.remove.member");
    }

    @Override
    public ResponseEntity<MemberDTO> insertMember(Integer groupId, RequestMemberDO requestMember) {
        throw new CommonException("error.add.member");
    }

    @Override
    public ResponseEntity<MemberDTO> updateMember(Integer groupId, RequestMemberDO requestMember) {
        throw new CommonException("error.update.member");
    }

    @Override
    public ResponseEntity<UserDO> createGitLabUser(String password, Integer projectsLimit, GitlabUserPayload gitlabUserPayload) {
        throw new CommonException("error.GitlabUser.creat");
    }

    @Override
    public ResponseEntity<UserDO> updateGitLabUser(Integer userId, Integer projectsLimit, GitlabUserPayload gitlabUserPayload) {
        throw new CommonException("error.GitlabUser.update");
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
    public ResponseEntity<List<DeployKey>> getDeploykeys(Integer projectId, Integer userId) {
        throw new CommonException("error.deploykey.get");
    }

    @Override
    public ResponseEntity<Map<String, Object>> addVariable(Integer projectId, String key, String value, Boolean protecteds, Integer userId) {
        throw new CommonException("error.variable.get");
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> batchAddVariable(Integer projectId, Integer userId, List<VariableDTO> variableDTOS) {
        throw new CommonException("error.variable.batch.create");
    }

    @Override
    public ResponseEntity deleteProject(Integer gitlabProjectId, Integer userId) {
        throw new CommonException("error.service.delete");
    }

    @Override
    public ResponseEntity deleteProjectByProjectName(String groupName, String projectName, Integer userId) {
        throw new CommonException("error.service.delete");
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> getProjectById(Integer projectId) {
        throw new CommonException("error.project.get");
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> getProjectByName(Integer userId, String groupName, String projectName) {
        throw new CommonException("error.project.get");
    }

    @Override
    public ResponseEntity<List<Variable>> getVariable(Integer projectId, Integer userId) {
        throw new CommonException("error.variable.get");
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
        throw new CommonException("error.pipelines.select");
    }

    @Override
    public ResponseEntity<List<JobDO>> listJobs(Integer projectId, Integer pipelineId, Integer userId) {
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
    public ResponseEntity<List<BranchDO>> listBranches(Integer projectId, Integer userId) {
        throw new CommonException("error.select.branch");
    }

    @Override
    public ResponseEntity<CommitDTO> getCommit(Integer projectId, String sha, Integer userId) {
        return new ResponseEntity("error.commit.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CommitDTO>> listCommits(Integer projectId, Integer page, Integer size, Integer userId) {
        throw new CommonException("error.commit.select");
    }

    @Override
    public ResponseEntity<List<CommitStatuseDO>> getCommitStatus(Integer projectId, String sha, Integer userId) {
        return new ResponseEntity("error.commitStatus.select", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<List<CommitDTO>> getCommits(Integer projectId, String branchName, String since) {
        throw new CommonException("error.commits.get");
    }

    @Override
    public ResponseEntity<GroupDO> createGroup(GroupDO groupDO, Integer userId) {
        throw new CommonException("error.group.create");
    }

    @Override
    public ResponseEntity<List<GitlabProjectDTO>> listProjects(Integer groupId, Integer userId) {
        throw new CommonException("error.group.listProjects");
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
        throw new CommonException("error.group.get");
    }

    @Override
    public ResponseEntity<RepositoryFile> createFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        throw new CommonException("error.file.create");
    }

    @Override
    public ResponseEntity<RepositoryFile> createFile(Integer projectId, String path, String content, String commitMessage, Integer userId, String branchName) {
        throw new CommonException("error.file.create");
    }

    @Override
    public ResponseEntity<RepositoryFile> updateFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        throw new CommonException("error.file.update");
    }

    @Override
    public ResponseEntity deleteFile(Integer projectId, String path, String commitMessage, Integer userId) {
        throw new CommonException("error.file.delete");
    }

    @Override
    public ResponseEntity<RepositoryFile> getFile(Integer projectId, String commit, String filePath) {
        return new ResponseEntity("error.file.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<CompareResultsE> getCompareResults(Integer projectId, String from, String to) {
        throw new CommonException("error.diffs.get");
    }

    @Override
    public ResponseEntity<ImpersonationTokenDO> create(Integer userId) {
        throw new CommonException("error.access_token.create");
    }

    @Override
    public ResponseEntity<Map<String, Object>> createProtectedBranches(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel, Integer userId) {
        throw new CommonException("error.branch.create");
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
    public ResponseEntity<MergeRequestDTO> createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, Integer userId) {
        throw new CommonException("error.mergeRequest.create");
    }

    @Override
    public ResponseEntity<MergeRequestDTO> acceptMergeRequest(Integer projectId, Integer mergeRequestId, String mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, Integer userId) {
        throw new CommonException("error.mergeRequext.accept");
    }

    @Override
    public ResponseEntity<List<TagDO>> getTags(Integer projectId, Integer userId) {
        throw new CommonException("error.tags.get");
    }

    @Override
    public ResponseEntity<TagDO> createTag(Integer projectId, String name, String ref,
                                           String msg, String releaseNotes, Integer userId) {
        throw new CommonException("error.tags.create");
    }

    @Override
    public ResponseEntity<TagDO> updateTagRelease(Integer projectId, String name, String releaseNotes, Integer userId) {
        throw new CommonException("error.tags.update");
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
    public ResponseEntity<BranchDO> getBranch(Integer projectId, String branchName) {
        throw new CommonException("error.branch.get");
    }

    @Override
    public ResponseEntity<BranchDO> createBranch(Integer projectId, String name, String source, Integer userId) {
        throw new CommonException("error.branch.create");
    }

    @Override
    public ResponseEntity<List<TagDO>> getPageTags(Integer projectId, int page, int perPage, Integer userId) {
        throw new CommonException("error.pageTags.get");
    }

    @Override
    public ResponseEntity enabledUserByUserId(Integer userId) {
        throw new CommonException("error.user.unblockUser");
    }

    @Override
    public ResponseEntity disEnabledUserByUserId(Integer userId) {
        throw new CommonException("error.user.blockUser");
    }

    @Override
    public ResponseEntity<ProjectHook> createProjectHook(Integer projectId, Integer userId, ProjectHook projectHook) {
        throw new CommonException("error.projecthook.create");
    }

    @Override
    public ResponseEntity<ProjectHook> updateProjectHook(Integer projectId, Integer hookId, Integer userId) {
        throw new CommonException("error.projecthook.update");
    }

    @Override
    public ResponseEntity<List<ProjectHook>> getProjectHook(Integer projectId, Integer userId) {
        throw new CommonException("error.projecthook.get");
    }

    @Override
    public ResponseEntity updateGroup(Integer groupId, Integer userId, GroupDO group) {
        throw new CommonException("error.group.update");
    }

    @Override
    public ResponseEntity addMemberIntoProject(Integer projectId, MemberVO memberDTO) {
        throw new CommonException("error.member.add");
    }

    @Override
    public ResponseEntity removeMemberFromProject(Integer projectId, Integer userId) {
        throw new CommonException("error.member.remove");
    }

    @Override
    public ResponseEntity updateMemberIntoProject(Integer projectId, List<MemberVO> list) {
        throw new CommonException("error.member.update");
    }

    @Override
    public ResponseEntity<MemberDTO> getProjectMember(Integer projectId, Integer userId) {
        throw new CommonException("error.project.member.get");
    }

    @Override
    public ResponseEntity getAllMemberByProjectId(Integer projectId) {
        throw new CommonException("error.project.member.list");
    }

    @Override
    public ResponseEntity<List<GitlabProjectDTO>> getProjectsByUserId(Integer userId) {
        throw new CommonException("error.project.get.by.userId");
    }

    @Override
    public ResponseEntity<Boolean> checkEmailIsExist(String email) {
        throw new CommonException("error.gitlab.user.email.check");
    }
}
