package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import io.choerodon.devops.domain.application.valueobject.RepositoryFile;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.feign.fallback.GitlabServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



/**
 * gitlab服务 feign客户端
 * Created by Zenger on 2018/3/28.
 */
@FeignClient(value = "gitlab-service", fallback = GitlabServiceClientFallback.class)
public interface GitlabServiceClient {
    @GetMapping(value = "/v1/users/{userId}")
    ResponseEntity<UserDTO> queryUserById(
            @PathVariable("userId") Integer userId);

    @GetMapping(value = "/v1/users/{username}/details")
    ResponseEntity<UserDTO> queryUserByUserName(
            @PathVariable("username") String username);

    @GetMapping(value = "/v1/groups/{groupId}/members/{userId}")
    ResponseEntity<MemberDTO> queryGroupMember(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId);

    @DeleteMapping(value = "/v1/groups/{groupId}/members/{userId}")
    ResponseEntity deleteMember(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId);

    @PostMapping(value = "/v1/groups/{groupId}/members")
    ResponseEntity<MemberDTO> createGroupMember(
            @PathVariable("groupId") Integer groupId,
            @RequestBody @Valid MemberDTO memberDTO);

    @PutMapping(value = "/v1/groups/{groupId}/members")
    ResponseEntity<MemberDTO> updateGroupMember(
            @PathVariable("groupId") Integer groupId,
            @RequestBody @Valid MemberDTO member);

    @PostMapping(value = "/v1/users")
    ResponseEntity<UserDTO> createUser(@RequestParam("password") String password,
                                       @RequestParam(value = "projectsLimit", required = false) Integer projectsLimit,
                                       @RequestBody UserReqDTO userReqDTO);

    @PutMapping("/v1/users/{userId}")
    ResponseEntity<UserDTO> updateGitLabUser(@PathVariable("userId") Integer userId,
                                             @RequestParam(value = "projectsLimit", required = false) Integer projectsLimit,
                                             @RequestBody UserReqDTO userReqDTO);


    @PutMapping("/v1/projects/{projectId}")
    ResponseEntity<GitlabProjectDTO> updateProject(@PathVariable("projectId") Integer projectId,
                                                   @RequestParam("userId") Integer userId);

    @PostMapping("/v1/projects")
    ResponseEntity<GitlabProjectDTO> createProject(@RequestParam("groupId") Integer groupId,
                                                   @RequestParam("projectName") String projectName,
                                                   @RequestParam("userId") Integer userId,
                                                   @RequestParam("visibility") boolean visibility);

    @PostMapping("/v1/projects/deploy_key")
    ResponseEntity createDeploykey(@RequestParam("projectId") Integer projectId,
                                   @RequestParam("title") String title,
                                   @RequestParam("key") String key,
                                   @RequestParam("canPush") boolean canPush,
                                   @RequestParam("userId") Integer userId);

    @GetMapping("/v1/projects/deploy_key")
    ResponseEntity<List<DeployKeyDTO>> listDeploykey(@RequestParam("projectId") Integer projectId,
                                                     @RequestParam("userId") Integer userId);


    @PostMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<Map<String, Object>> addProjectVariable(@PathVariable("projectId") Integer projectId,
                                                           @RequestParam("key") String key,
                                                           @RequestParam("value") String value,
                                                           @RequestParam("protecteds") Boolean protecteds,
                                                           @RequestParam("userId") Integer userId);

    @PutMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<List<Map<String, Object>>> batchAddProjectVariable(@PathVariable("projectId") Integer projectId,
                                                                      @RequestParam("userId") Integer userId,
                                                                      @RequestBody @Valid List<VariableDTO> variableDTODTOS);

    @DeleteMapping(value = "/v1/projects/{projectId}")
    ResponseEntity deleteProjectById(@PathVariable("projectId") Integer projectId,
                                     @RequestParam("userId") Integer userId);

    @DeleteMapping(value = "/v1/projects/{groupName}/{projectName}")
    ResponseEntity deleteProjectByName(@PathVariable("groupName") String groupName,
                                       @PathVariable("projectName") String projectName,
                                       @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{project_id}")
    ResponseEntity<GitlabProjectDTO> queryProjectById(@PathVariable("project_id") Integer projectId);

    @GetMapping(value = "/v1/projects/queryByName")
    ResponseEntity<GitlabProjectDTO> queryProjectByName(@RequestParam("userId") Integer userId,
                                                        @RequestParam("groupName") String groupName,
                                                        @RequestParam("projectName") String projectName);


    @GetMapping(value = "/v1/projects/{projectId}/variable")
    ResponseEntity<List<VariableDTO>> listVariable(@PathVariable("projectId") Integer projectId,
                                                   @RequestParam("userId") Integer userId);


    @PostMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<ImpersonationTokenDO> create(@PathVariable("userId") Integer userId);

    @PostMapping(value = "/v1/groups")
    ResponseEntity<GroupDTO> createGroup(
            @RequestBody @Valid GroupDTO group,
            @RequestParam("userId") Integer userId
    );

    @GetMapping(value = "/v1/groups/{groupId}/projects/event")
    ResponseEntity<List<GitlabProjectDTO>> listProjects(@PathVariable("groupId") Integer groupId,
                                                        @RequestParam(value = "userId", required = false) Integer userId);

    @PostMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<ImpersonationTokenDO> createProjectToken(@PathVariable("userId") Integer userId);

    @GetMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<List<ImpersonationTokenDO>> listProjectToken(@PathVariable("userId") Integer userId);

    @GetMapping(value = "/v1/groups/{groupName}")
    ResponseEntity<GroupDTO> queryGroupByName(@PathVariable("groupName") String groupName,
                                              @RequestParam(value = "userId") Integer userId);

    @PostMapping(value = "/v1/projects/{projectId}/repository/file")
    ResponseEntity<RepositoryFile> createFile(@PathVariable("projectId") Integer projectId,
                                              @RequestParam("path") String path,
                                              @RequestParam("content") String content,
                                              @RequestParam("commitMessage") String commitMessage,
                                              @RequestParam("userId") Integer userId);

    @PostMapping(value = "/v1/projects/{projectId}/repository/file")
    ResponseEntity<RepositoryFile> createFile(@PathVariable("projectId") Integer projectId,
                                              @RequestParam("path") String path,
                                              @RequestParam("content") String content,
                                              @RequestParam("commitMessage") String commitMessage,
                                              @RequestParam("userId") Integer userId,
                                              @RequestParam("branch_name") String branchName);

    @PutMapping(value = "/v1/projects/{projectId}/repository/file")
    ResponseEntity<RepositoryFile> updateFile(@PathVariable("projectId") Integer projectId,
                                              @RequestParam("path") String path,
                                              @RequestParam("content") String content,
                                              @RequestParam("commitMessage") String commitMessage,
                                              @RequestParam("userId") Integer userId);

    @DeleteMapping(value = "/v1/projects/{projectId}/repository/file")
    ResponseEntity deleteFile(@PathVariable("projectId") Integer projectId,
                              @RequestParam("path") String path,
                              @RequestParam("commitMessage") String commitMessage,
                              @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/{commit}/file")
    ResponseEntity<RepositoryFile> getFile(@PathVariable("projectId") Integer projectId,
                                           @PathVariable("commit") String commit,
                                           @RequestParam(value = "file_path") String filePath);

    @GetMapping(value = "/v1/projects/{projectId}/repository/file/diffs")
    ResponseEntity<CompareResultDTO> queryCompareResult(@PathVariable("projectId") Integer projectId,
                                                        @RequestParam("from") String from,
                                                        @RequestParam("to") String to);

    @PostMapping(value = "/v1/projects/{projectId}/protected_branches")
    ResponseEntity<Map<String, Object>> createProtectedBranch(@PathVariable("projectId") Integer projectId,
                                                              @RequestParam("name") String name,
                                                              @RequestParam("mergeAccessLevel") String mergeAccessLevel,
                                                              @RequestParam("pushAccessLevel") String pushAccessLevel,
                                                              @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines")
    ResponseEntity<List<PipelineDTO>> listPipeline(@PathVariable("projectId") Integer projectId,
                                                   @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/page")
    ResponseEntity<List<PipelineDTO>> pagePipeline(@PathVariable("projectId") Integer projectId,
                                                   @RequestParam("page") Integer page,
                                                   @RequestParam("size") Integer size,
                                                   @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}")
    ResponseEntity<PipelineDTO> queryPipeline(@PathVariable("projectId") Integer projectId,
                                              @PathVariable("pipelineId") Integer pipelineId,
                                              @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits")
    ResponseEntity<CommitDTO> queryCommit(@PathVariable("projectId") Integer projectId,
                                          @RequestParam("sha") String sha,
                                          @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits/project")
    ResponseEntity<List<CommitDTO>> listCommits(@PathVariable("projectId") Integer projectId,
                                                @RequestParam("page") Integer page,
                                                @RequestParam("size") Integer size,
                                                @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits/statuse")
    ResponseEntity<List<CommitStatuseDTO>> listCommitStatus(@PathVariable("projectId") Integer projectId,
                                                            @RequestParam("sha") String sha,
                                                            @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits/branch")
    ResponseEntity<List<CommitDTO>> getCommits(@PathVariable("projectId") Integer projectId,
                                               @RequestParam("branchName") String branchName,
                                               @RequestParam("since") String since);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/jobs")
    ResponseEntity<List<JobDTO>> listJobs(@PathVariable("projectId") Integer projectId,
                                          @PathVariable("pipelineId") Integer pipelineId,
                                          @RequestParam("userId") Integer userId);

    @PutMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}")
    ResponseEntity updateMergeRequest(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer merRequestId,
            @RequestParam(value = "userId", required = false) Integer userId);

    @GetMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}")
    ResponseEntity<MergeRequestDTO> getMergeRequest(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer mergeRequestId,
            @RequestParam(value = "userId", required = false) Integer userId);

    @GetMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}/commit")
    ResponseEntity<List<CommitDTO>> listCommits(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer mergeRequestId,
            @RequestParam(value = "userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/merge_requests")
    ResponseEntity<List<MergeRequestDTO>> getMergeRequestList(@PathVariable("projectId") Integer projectId);

    @GetMapping("/v1/projects/{projectId}/repository/branches")
    ResponseEntity<List<BranchDTO>> listBranch(@PathVariable("projectId") Integer projectId,
                                               @RequestParam(value = "userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/retry")
    ResponseEntity<PipelineDTO> retryPipeline(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("pipelineId") Integer pipelineId,
            @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/cancel")
    ResponseEntity<PipelineDTO> cancelPipeline(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("pipelineId") Integer pipelineId,
            @RequestParam("userId") Integer userId);


    /**
     * 创建merge请求
     *
     * @param projectId    工程ID
     * @param sourceBranch 源分支
     * @param targetBranch 目标分支
     * @param title        标题
     * @param description  描述
     * @return 创建的merge请求
     */
    @PostMapping("/v1/projects/{projectId}/merge_requests")
    ResponseEntity<MergeRequestDTO> createMergeRequest(
            @PathVariable("projectId") Integer projectId,
            @RequestParam("sourceBranch") String sourceBranch,
            @RequestParam("targetBranch") String targetBranch,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "userId", required = false) Integer userId);

    /**
     * 执行merge请求
     *
     * @param projectId                工程ID
     * @param mergeRequestId           mergeRequest的ID
     * @param mergeCommitMessage       commit信息
     * @param shouldRemoveSourceBranch merge后是否删除源分支
     * @return merge请求
     */
    @PutMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}/merge")
    ResponseEntity<MergeRequestDTO> acceptMergeRequest(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer mergeRequestId,
            @RequestParam("mergeCommitMessage") String mergeCommitMessage,
            @RequestParam("removeSourceBranch") Boolean shouldRemoveSourceBranch,
            @RequestParam("mergeWhenPipelineSucceeds") Boolean mergeWhenPipelineSucceeds,
            @RequestParam(value = "userId", required = false) Integer userId);

    /**
     * 获取tag列表
     *
     * @param projectId 工程ID
     * @return tag列表
     */
    @GetMapping("/v1/projects/{projectId}/repository/tags")
    ResponseEntity<List<TagDTO>> getTags(
            @PathVariable("projectId") Integer projectId,
            @RequestParam(value = "userId", required = false) Integer userId);

    /**
     * 创建tag
     *
     * @param projectId 工程ID
     * @param name      tag名称
     * @param ref       创建tag的源
     * @return 创建的tag
     */
    @PostMapping("/v1/projects/{projectId}/repository/tags")
    ResponseEntity<TagDTO> createTag(
            @PathVariable("projectId") Integer projectId,
            @RequestParam("name") String name,
            @RequestParam("ref") String ref,
            @RequestParam(value = "message", required = false, defaultValue = "") String message,
            @RequestBody(required = false) String releaseNotes,
            @RequestParam("userId") Integer userId);

    /**
     * 更新 tag
     *
     * @param projectId    项目id
     * @param name         标签名
     * @param releaseNotes 发布日志
     * @return Tag
     */
    @PutMapping("/v1/projects/{projectId}/repository/tags")
    ResponseEntity<TagDTO> updateTag(
            @PathVariable("projectId") Integer projectId,
            @RequestParam("name") String name,
            @RequestBody(required = false) String releaseNotes,
            @RequestParam("userId") Integer userId);

    /**
     * 删除tag
     *
     * @param projectId 工程ID
     * @param name      tag名称
     */
    @DeleteMapping("/v1/projects/{projectId}/repository/tags")
    ResponseEntity deleteTag(
            @PathVariable("projectId") Integer projectId,
            @RequestParam("name") String name,
            @RequestParam("userId") Integer userId);

    @DeleteMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}")
    ResponseEntity deleteMergeRequest(@PathVariable("projectId") Integer projectId,
                                      @PathVariable("mergeRequestId") Integer mergeRequestId);

    /**
     * 根据分支名删除分支
     *
     * @param projectId  工程ID
     * @param branchName 分支名
     * @return 不含任何消息体的ResponseEntity
     */
    @DeleteMapping("/v1/projects/{projectId}/repository/branches")
    ResponseEntity<Object> deleteBranch(
            @PathVariable("projectId") Integer projectId,
            @RequestParam("branchName") String branchName,
            @RequestParam("userId") Integer userId);


    /**
     * 根据分支名查询分支
     *
     * @param projectId  工程ID
     * @param branchName 分支名
     * @return 不含任何消息体的ResponseEntity
     */
    @GetMapping("/v1/projects/{projectId}/repository/branches/{branchName}")
    ResponseEntity<BranchDTO> queryBranch(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("branchName") String branchName);


    /**
     * 创建新分支的接口
     *
     * @param projectId 工程ID
     * @param name      创建的分支名
     * @param source    源分支名
     * @return 创建的分支
     */
    @PostMapping("/v1/projects/{projectId}/repository/branches")
    ResponseEntity<BranchDTO> createBranch(
            @PathVariable("projectId") Integer projectId,
            @RequestParam("name") String name,
            @RequestParam("source") String source,
            @RequestParam("userId") Integer userId);

    /**
     * 获取tag列表
     *
     * @param projectId 工程ID
     * @param page      页数
     * @param perPage   每页个数
     * @return tag列表
     */
    @GetMapping("/v1/projects/{projectId}/repository/tags/page")
    ResponseEntity<List<TagDTO>> getPageTags(@PathVariable("projectId") Integer projectId,
                                             @RequestParam("page") int page,
                                             @RequestParam("perPage") int perPage,
                                             @RequestParam("userId") Integer userId);

    @PutMapping("/v1/users/{userId}/is_enabled")
    ResponseEntity enableUser(@PathVariable("userId") Integer userId);

    @PutMapping("/v1/users/{userId}/dis_enabled")
    ResponseEntity disableUser(@PathVariable("userId") Integer userId);

    @PostMapping("/v1/hook")
    ResponseEntity<ProjectHookDTO> createProjectHook(
            @RequestParam("projectId") Integer projectId,
            @RequestParam("userId") Integer userId,
            @RequestBody ProjectHookDTO projectHookDTO);

    @PutMapping("/v1/hook")
    ResponseEntity<ProjectHookDTO> updateProjectHook(
            @RequestParam("projectId") Integer projectId,
            @RequestParam("hookId") Integer hookId,
            @RequestParam("userId") Integer userId);

    @GetMapping("/v1/hook")
    ResponseEntity<List<ProjectHookDTO>> listProjectHook(
            @RequestParam("projectId") Integer projectId,
            @RequestParam("userId") Integer userId);

    @PutMapping("/v1/groups/{groupId}")
    ResponseEntity updateGroup(@PathVariable("groupId") Integer groupId,
                               @RequestParam("userId") Integer userId,
                               @RequestBody @Valid GroupDTO group);

    @PostMapping("/v1/projects/{projectId}/members")
    ResponseEntity createProjectMember(@PathVariable("projectId") Integer projectId,
                                       @RequestBody MemberDTO memberDTO);

    @PutMapping("/v1/projects/{projectId}/members")
    ResponseEntity updateProjectMember(@PathVariable("projectId") Integer projectId,
                                       @RequestBody List<MemberDTO> list);

    @GetMapping("/v1/projects/{projectId}/members/{userId}")
    ResponseEntity<MemberDTO> getProjectMember(@PathVariable("projectId") Integer projectId,
                                               @PathVariable("userId") Integer userId);

    @DeleteMapping("/v1/projects/{projectId}/members/{userId}")
    ResponseEntity deleteProjectMember(@PathVariable("projectId") Integer projectId,
                                       @PathVariable("userId") Integer userId);

    @GetMapping("/v1/projects/{project_id}/members/list")
    ResponseEntity<List<MemberDTO>> listMemberByProject(@PathVariable(value = "project_id") Integer projectId);

    @GetMapping("/v1/projects/{user_id}/projects")
    ResponseEntity<List<GitlabProjectDTO>> listProjectByUser(@PathVariable(value = "user_id") Integer id);

    @GetMapping("/v1/users/email/check")
    ResponseEntity<Boolean> checkEmail(@RequestParam(value = "email") String email);
}
