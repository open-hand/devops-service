package io.choerodon.devops.infra.feign.fallback;

import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.*;
import static io.choerodon.devops.infra.constant.ExceptionConstants.GitopsCode.DEVOPS_FILE_CREATE;
import static io.choerodon.devops.infra.constant.ExceptionConstants.GitopsCode.DEVOPS_FILE_UPDATE;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiVariableVO;
import io.choerodon.devops.api.vo.FileCreationVO;
import io.choerodon.devops.infra.dto.RepositoryFileDTO;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.dto.gitlab.ci.Pipeline;
import io.choerodon.devops.infra.feign.GitlabServiceClient;


/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabServiceClientFallback implements GitlabServiceClient {

    @Override
    public ResponseEntity<RepositoryFileDTO> createFile(Integer projectId, FileCreationVO fileCreationVO, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException(DEVOPS_FILE_CREATE);
    }

    @Override
    public ResponseEntity<RepositoryFileDTO> updateFile(Integer projectId, FileCreationVO fileCreationVO, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException(DEVOPS_FILE_UPDATE);
    }

    @Override
    public ResponseEntity deleteFile(Integer projectId, FileCreationVO fileCreationVO, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.file.delete");
    }

    @Override
    public ResponseEntity<GitLabUserDTO> queryUserById(Integer userId) {
        throw new CommonException(DEVOPS_USER_GET);
    }

    @Override
    public ResponseEntity<GitLabUserDTO> queryUserByUserName(String username) {
        throw new CommonException(DEVOPS_USER_GET);
    }

    @Override
    public ResponseEntity<MemberDTO> queryGroupMember(Integer groupId, Integer userId) {
        throw new CommonException("devops.group.member.get");
    }

    @Override
    public ResponseEntity<List<MemberDTO>> listGroupMember(Integer groupId) {
        throw new CommonException("devops.group.member.list");
    }

    @Override
    public ResponseEntity<Page<MemberDTO>> pageMember(Integer groupId, Integer page, Integer size, Integer userId, String search) {
        throw new CommonException("devops.group.member.list");
    }

    @Override
    public ResponseEntity deleteMember(Integer groupId, Integer userId) {
        throw new CommonException("devops.remove.member");
    }

    @Override
    public ResponseEntity<MemberDTO> createGroupMember(Integer groupId, MemberDTO requestMember) {
        throw new CommonException("devops.add.member");
    }

    @Override
    public ResponseEntity<MemberDTO> updateGroupMember(Integer groupId, MemberDTO requestMember) {
        throw new CommonException("devops.update.member");
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        throw new CommonException("devops.project.create");
    }

    @Override
    public ResponseEntity createDeploykey(Integer projectId, GitlabTransferDTO gitlabTransferDTO, boolean canPush, Integer userId) {
        throw new CommonException("devops.deploykey.create");
    }

    @Override
    public ResponseEntity<List<DeployKeyDTO>> listDeploykey(Integer projectId, Integer userId) {
        throw new CommonException("devops.deploykey.get");
    }

    @Override
    public ResponseEntity<CiVariableVO> addProjectVariable(Integer projectId, GitlabTransferDTO gitlabTransferDTO, Boolean protecteds, Integer userId) {
        throw new CommonException("devops.variable.get");
    }

    @Override
    public ResponseEntity deleteProjectById(Integer gitlabProjectId, Integer userId) {
        throw new CommonException("devops.service.delete");
    }

    @Override
    public ResponseEntity deleteProjectByName(String groupName, String projectName, Integer userId) {
        throw new CommonException("devops.service.delete");
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> queryProjectById(Integer projectId) {
        throw new CommonException("devops.project.query.by.id", projectId);
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> queryProjectByName(Integer userId, String groupName, String projectName, Boolean statistics) {
        throw new CommonException("devops.project.get");
    }

    @Override
    public ResponseEntity<List<CiVariableVO>> listAppServiceVariable(Integer projectId, Integer userId) {
        throw new CommonException("devops.devops.ci.appService.variable.list");
    }

    @Override
    public ResponseEntity<List<CiVariableVO>> listProjectVariable(Integer projectId, Integer userId) {
        throw new CommonException("devops.devops.ci.global.variable.list");
    }

    @Override
    public ResponseEntity<List<GitlabPipelineDTO>> listPipeline(Integer projectId, Integer userId) {
        throw new CommonException("devops.pipeline.select");
    }

    @Override
    public ResponseEntity<List<GitlabPipelineDTO>> pagePipeline(Integer projectId, Integer page, Integer size, Integer userId) {
        throw new CommonException("devops.pipelines.select");
    }

    @Override
    public ResponseEntity<GitlabPipelineDTO> queryPipeline(Integer projectId, Integer pipelineId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.pipelines.select");
    }

    @Override
    public ResponseEntity<List<JobDTO>> listJobs(Integer projectId, Integer pipelineId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.job.select");
    }

    @Override
    public ResponseEntity updateMergeRequest(Integer projectId, Integer merRequestId, Integer userId) {
        throw new CommonException("devops.mr.update");
    }

    @Override
    public ResponseEntity<MergeRequestDTO> getMergeRequest(Integer projectId, Integer mergeRequestId, Integer userId) {
        throw new CommonException("devops.mr.get");
    }

    @Override
    public ResponseEntity<List<CommitDTO>> listCommits(Integer projectId, Integer mergeRequestId, Integer userId) {
        throw new CommonException("devops.mr.commits.get");
    }

    @Override
    public ResponseEntity<List<MergeRequestDTO>> getMergeRequestList(Integer projectId) {
        throw new CommonException("devops.mergerequest.get");
    }

    @Override
    public ResponseEntity<List<BranchDTO>> listBranch(Integer projectId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.select.branch");
    }

    @Override
    public ResponseEntity<CommitDTO> queryCommit(Integer projectId, String sha, Integer userId) {

        throw new CommonException("devops.commit.select");
    }

    @Override
    public ResponseEntity<List<CommitDTO>> listCommits(Integer projectId, Integer page, Integer size, Integer
            userId) {
        throw new CommonException("devops.commit.select");
    }

    @Override
    public ResponseEntity<List<CommitDTO>> getCommits(Integer projectId, GitlabTransferDTO gitlabTransferDTO) {
        throw new CommonException("devops.commits.get");
    }

    @Override
    public ResponseEntity<List<CommitDTO>> getCommitsByRef(Integer projectId, String ref) {
        throw new CommonException("devops.commits.get");
    }

    @Override
    public ResponseEntity<GroupDTO> createGroup(GroupDTO groupDO, Integer userId) {
        throw new CommonException("devops.group.create");
    }

    @Override
    public ResponseEntity<List<GitlabProjectDTO>> listProjects(Integer groupId, Integer userId, Integer page, Integer perPage) {
        throw new CommonException("devops.group.listProjects");
    }

    @Override
    public ResponseEntity<GroupDTO> queryGroupByName(String groupName, Integer userId) {
        throw new CommonException("devops.group.get");
    }

    @Override
    public ResponseEntity<GroupDTO> queryGroupByIid(Integer groupIid, Integer userId) {
        throw new CommonException("devops.group.get");
    }

    @Override
    public ResponseEntity<RepositoryFileDTO> getFile(Integer projectId, String commit, String filePath) {
        throw new CommonException("devops.file.get");
    }

    @Override
    public ResponseEntity<RepositoryFileDTO> getExternalFile(Integer projectId, String commit, String filePath, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.file.get");
    }


    @Override
    public ResponseEntity<ImpersonationTokenDTO> create(Integer userId) {
        throw new CommonException("devops.access_token.create");
    }


    @Override
    public ResponseEntity<MergeRequestDTO> createMergeRequest(Integer projectId, GitlabTransferDTO gitlabTransferDTO, Integer userId) {
        throw new CommonException("devops.mergeRequest.create");
    }

    @Override
    public ResponseEntity<MergeRequestDTO> acceptMergeRequest(Integer projectId, Integer mergeRequestId, String
            mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, Integer userId) {
        throw new CommonException("devops.mergeRequext.accept");
    }

    @Override
    public ResponseEntity<List<TagDTO>> getTags(Integer projectId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException(DEVOPS_TAGS_GET);
    }


    @Override
    public ResponseEntity<TagDTO> createTag(Integer projectId, GitlabTransferDTO gitlabTransferDTO, Integer userId) {
        throw new CommonException("devops.tags.create");
    }

    @Override
    public ResponseEntity<Release> createRelease(Integer projectId, Integer userId, ReleaseParams release) {
        throw new CommonException("devops.release.create");
    }

    @Override
    public ResponseEntity<Release> updateRelease(Integer projectId, Integer userId, ReleaseParams release) {
        throw new CommonException("devops.release.update");
    }

    @Override
    public ResponseEntity<Release> queryRelease(Integer projectId, Integer userId, String tagName) {
        throw new CommonException("devops.release.query");
    }

    @Override
    public ResponseEntity deleteTag(Integer projectId, String name, Integer userId) {
        throw new CommonException("devops.tag.delete");
    }

    @Override
    public ResponseEntity deleteMergeRequest(Integer projectId, Integer mergeRequestId) {
        throw new CommonException("devops.mergeRequest.delete");
    }

    @Override
    public ResponseEntity<Object> deleteBranch(Integer projectId, String branchName, Integer userId) {
        throw new CommonException("devops.branch.delete");
    }

    @Override
    public ResponseEntity<BranchDTO> queryBranch(Integer projectId, String branchName) {
        throw new CommonException(DEVOPS_BRANCH_GET);
    }

    @Override
    public ResponseEntity<BranchDTO> createBranch(Integer projectId, GitlabTransferDTO gitlabTransferDTO, Integer userId) {
        throw new CommonException(DEVOPS_BRANCH_CREATE);
    }

    @Override
    public ResponseEntity<List<TagDTO>> getPageTags(Integer projectId, int page, int perPage, Integer userId) {
        throw new CommonException("devops.pageTag.get");
    }

    @Override
    public ResponseEntity<ProjectHookDTO> createProjectHook(Integer projectId, Integer userId, ProjectHookDTO
            projectHook) {
        throw new CommonException(DEVOPS_PROJECTHOOK_CREATE);
    }

    @Override
    public ResponseEntity<String> createExternalProjectHook(Integer projectId, ProjectHookDTO projectHookDTO, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException(DEVOPS_PROJECTHOOK_CREATE);
    }

    @Override
    public ResponseEntity updateGroup(Integer groupId, Integer userId, GroupDTO group) {
        throw new CommonException("devops.group.update");
    }

    @Override
    public ResponseEntity<GitLabUserDTO> createUser(Integer projectsLimit, GitlabTransferDTO gitlabTransferDTO) {
        throw new CommonException("devops.gitlab.user.create");
    }

    @Override
    public ResponseEntity<GitLabUserDTO> updateGitLabUser(Integer userId, Integer projectsLimit, GitlabUserReqDTO userReqDTO) {
        throw new CommonException("devops.gitlab.user.update");
    }

    @Override
    public ResponseEntity<GitLabUserDTO> updateUserPasswordByUserId(Integer userId, GitlabUserWithPasswordDTO user) {
        throw new CommonException("devops.reset.user.password");
    }

    @Override
    public ResponseEntity<List<CiVariableVO>> batchSaveProjectVariable(Integer projectId, Integer userId, @Valid List<CiVariableVO> ciVariableVOList) {
        throw new CommonException("devops.variable.create");
    }

    @Override
    public ResponseEntity<ImpersonationTokenDTO> createProjectToken(Integer userId, String tokenName, Date date) {
        throw new CommonException("devops.project.token.create");
    }

    @Override
    public ResponseEntity<Void> revokeImpersonationToken(Integer userId, Integer tokenId) {
        throw new CommonException("devops.project.token.revoke");
    }

    @Override
    public ResponseEntity<List<ImpersonationTokenDTO>> listProjectToken(Integer userId) {
        throw new CommonException("devops.project.token.list");
    }

    @Override
    public ResponseEntity<CompareResultDTO> queryCompareResult(Integer projectId, GitlabTransferDTO gitlabTransferDTO) {
        throw new CommonException("devops.compare.query");
    }

    @Override
    public ResponseEntity<Map<String, Object>> createProtectedBranch(Integer projectId, GitlabTransferDTO gitlabTransferDTO, Integer userId) {
        throw new CommonException("devops.compare.query");
    }

    @Override
    public ResponseEntity<List<CommitStatusDTO>> listCommitStatus(Integer projectId, String sha, Integer userId) {
        throw new CommonException("devops.compare.list");
    }

    @Override
    public ResponseEntity<List<CommitDTO>> listExternalCommits(Integer projectId, Integer page, Integer size, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.commit.list");
    }

    @Override
    public ResponseEntity<Pipeline> retryPipeline(Integer projectId, Integer pipelineId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.pipeline.retry");
    }

    @Override
    public ResponseEntity<Pipeline> cancelPipeline(Integer projectId, Integer pipelineId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.pipeline.cancel");
    }

    @Override
    public ResponseEntity<TagDTO> updateTag(Integer projectId, GitlabTransferDTO gitlabTransferDTO, Integer userId) {
        throw new CommonException("devops.tag.update");
    }

    @Override
    public ResponseEntity enableUser(Integer userId) {
        throw new CommonException("devops.gitlab.user.enable");
    }

    @Override
    public ResponseEntity disableUser(Integer userId) {
        throw new CommonException("devops.gitlab.user.disable");
    }

    @Override
    public ResponseEntity<List<ProjectHookDTO>> listProjectHook(Integer projectId, Integer userId) {
        throw new CommonException("devops.gitlab.query");
    }

    @Override
    public ResponseEntity<String> listExternalHooks(Integer projectId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.listHooks.error");
    }

    @Override
    public ResponseEntity<Void> deleteExternalHook(Integer projectId, Integer hookId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.deleteHook.error");
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
    public ResponseEntity<MemberDTO> getProjectAllMember(Integer projectId, Integer userId) {
        throw new CommonException("devops.query.project.member");
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
    public ResponseEntity<List<MemberDTO>> getAllMemberByProjectIdAndQuery(Integer projectId, String query) {
        throw new CommonException("devops.query.project.members");
    }

    @Override
    public ResponseEntity<List<GitlabProjectDTO>> listProjectByUser(Integer id) {
        return null;
    }

    @Override
    public ResponseEntity<Boolean> checkEmail(String email) {
        return null;
    }

    @Override
    public ResponseEntity<GitLabUserDTO> queryUserByEmail(String email) {
        return null;
    }

    @Override
    public ResponseEntity<String> getAdminToken() {
        throw new CommonException("devops.admin.token.get");
    }

    @Override
    public ResponseEntity<Boolean> checkIsAdmin(Integer userId) {
        return new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> assignAdmin(Integer userId) {
        return new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> deleteAdmin(Integer userId) {
        return new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AccessRequestDTO>> listAccessRequestsOfGroup(Integer groupId) {
        throw new CommonException("devops.failed.to.query.group.access.request", groupId);
    }

    @Override
    public ResponseEntity denyAccessRequest(Integer groupId, Integer userId) {
        return new ResponseEntity(HttpStatus.OK);
    }

    @Override
    public ResponseEntity createCommit(Integer projectId, Integer userId, CommitPayloadDTO commitPayloadDTO) {
        throw new CommonException("devops.manipulate.gitlab.files");
    }

    @Override
    public ResponseEntity<Pipeline> createPipeline(Integer projectId, Integer userId, String ref, String gitlabUrl, String authType, String accessToken, String username, String password, Map<String, String> variables) {
        throw new CommonException("devops.pipeline.create");
    }

    @Override
    public ResponseEntity<String> queryTrace(Integer projectId, Integer jobId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.job.trace.get");
    }

    @Override
    public ResponseEntity<JobDTO> retryJob(Integer projectId, Integer jobId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.gitlab.job.retry");
    }

    @Override
    public ResponseEntity<JobDTO> playJob(Integer projectId, Integer jobId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.gitlab.job.play");
    }

    @Override
    public ResponseEntity<JobDTO> queryJob(Integer projectId, Integer jobId) {
        throw new CommonException("devops.gitlab.job.query");
    }

    @Override
    public ResponseEntity<GitLabUserDTO> queryAdminUser() {
        throw new CommonException("devops.gitlab.admin.id.query");
    }

    @Override
    public ResponseEntity<CiVariableVO> createGroupVariable(Integer groupId, GitlabTransferDTO gitlabTransferDTO, boolean protecteds, Integer userId) {
        throw new CommonException("devops.gitlab.create.group.variable");
    }

    @Override
    public ResponseEntity<List<CiVariableVO>> batchSaveGroupVariable(Integer groupId, Integer userId, List<CiVariableVO> list) {
        throw new CommonException("devops.gitlab.batch.create.group.variable");
    }

    @Override
    public ResponseEntity<Void> deleteVariable(Integer groupId, Integer userId, String key) {
        throw new CommonException("devops.gitlab.delete.group.variable");
    }

    @Override
    public ResponseEntity<Void> batchGroupDeleteVariable(Integer groupId, Integer userId, List<String> key) {
        throw new CommonException("devops.gitlab.batch.delete.group.variable");
    }

    @Override
    public ResponseEntity<Void> batchProjectDeleteVariable(Integer projectId, Integer userId, List<String> key) {
        throw new CommonException("devops.gitlab.batch.delete.project.variable");
    }

    @Override
    public ResponseEntity<List<Variable>> listExternalProjectVariable(Integer projectId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.listExternalProjectVariable.error");
    }

    @Override
    public ResponseEntity<Void> deleteExternalVariable(Integer projectId, String key, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.deleteExternalVariable.error");
    }

    @Override
    public ResponseEntity<List<Long>> listIds(Integer projectId) {
        throw new CommonException("devops.gitlab.mergeRequest.ids.list");
    }

    @Override
    public ResponseEntity<String> updateProject(Integer userId, Project project) {
        throw new CommonException("devops.update.gitlab.project");
    }

    @Override
    public ResponseEntity<Project> updateNameAndPath(Integer projectId, Integer userId, String name) {
        throw new CommonException("devops.update.gitlab.project");
    }

    @Override
    public ResponseEntity<List<Note>> listByMergeRequestIid(Integer projectId, Integer iid) {
        throw new CommonException("devops.query.mr.notes");
    }

    @Override
    public ResponseEntity<List<GroupDTO>> listGroupsWithParam(Integer userId, Boolean owned, String search, List<Integer> skipGroups) {
        throw new CommonException("devops.query.group");
    }

    @Override
    public ResponseEntity<Page<GroupDTO>> pagingGroupWithParam(Integer userId, Boolean owned, String search, Integer page, Integer size, Integer minAccessLevel, List<Integer> skipGroups) {
        throw new CommonException("devops.query.group");
    }

    @Override
    public ResponseEntity<List<GitlabProjectDTO>> listProjects(Integer groupId, Integer userId, Boolean owned, String search, Integer page, Integer perPage, Integer minAccessLevel) {
        throw new CommonException("devops.query.group.project");
    }


    @Override
    public ResponseEntity<GitlabProjectDTO> transferProject(Integer projectId, Integer userId, Integer groupId) {
        throw new CommonException("devops.transfer.projects");
    }

    @Override
    public ResponseEntity<InputStream> downloadArchiveByFormat(Integer projectId, Integer userId, String commitSha, String format) {
        throw new CommonException("devops.down.project");
    }

    @Override
    public ResponseEntity<GitlabProjectDTO> queryExternalProjectByCode(String namespaceCode, String projectCode, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.query.external.project");
    }

    @Override
    public ResponseEntity<List<CiVariableVO>> batchSaveExternalProjectVariable(Integer projectId, List<CiVariableVO> ciVariableVOList, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.save.external.project");
    }

    @Override
    public ResponseEntity<List<GroupDTO>> queryGroupWithStatisticsByName(String groupName, Integer userId, Boolean statistics) {
        throw new CommonException("devops.query.group.statistics");
    }

    @Override
    public ResponseEntity<PipelineSchedule> createPipelineSchedule(Integer projectId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password, PipelineSchedule pipelineSchedule) {
        throw new CommonException("devops.create.Pipeline.Schedule");
    }

    @Override
    public ResponseEntity<Variable> createScheduleVariable(Integer projectId, Integer pipelineScheduleId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password, Variable variable) {
        throw new CommonException("devops.create.Pipeline.Variable");
    }

    @Override
    public ResponseEntity<Variable> editScheduleVariable(Integer projectId, Integer pipelineScheduleId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password, Variable variable) {
        throw new CommonException("devops.edit.Pipeline.Variable");
    }

    @Override
    public ResponseEntity<Variable> deleteScheduleVariable(Integer projectId, Integer pipelineScheduleId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password, Variable variable) {
        throw new CommonException("devops.delete.Pipeline.Variable");
    }

    @Override
    public ResponseEntity<PipelineSchedule> queryPipelineSchedule(Integer projectId, Integer userId, Integer pipelineScheduleId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.query.Pipeline.Schedule");
    }

    @Override
    public ResponseEntity<List<PipelineSchedule>> listPipelineSchedules(Integer projectId, Integer userId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.list.Pipeline.Schedule");
    }

    @Override
    public ResponseEntity<Void> updatePipelineSchedule(Integer projectId, Integer userId, Integer pipelineScheduleId, String gitlabUrl, String authType, String accessToken, String username, String password, PipelineSchedule pipelineSchedule) {
        throw new CommonException("devops.update.Pipeline.Schedule");
    }

    @Override
    public ResponseEntity<Void> deletePipelineSchedule(Integer projectId, Integer userId, Integer pipelineScheduleId, String gitlabUrl, String authType, String accessToken, String username, String password) {
        throw new CommonException("devops.delete.Pipeline.Schedule");
    }

    @Override
    public ResponseEntity<List<GitLabUserDTO>> listAdminUsers() {
        throw new CommonException("devops.list.admin.users");
    }

    @Override
    public ResponseEntity<ProjectHookDTO> updateWebHook(Integer projectId, Integer userId, Integer hookId, ProjectHookDTO projectHookDTO) {
        throw new CommonException("devops.updateWebHook");
    }

    @Override
    public List<MergeRequestDTO> listMergeRequest(Integer projectId, String state) {
        throw new CommonException("devops.open.mergereuqest.list");
    }
}
