package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import io.choerodon.devops.api.vo.gitlab.MemberVO;
import io.choerodon.devops.api.vo.gitlab.VariableDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.CompareResultsE;
import io.choerodon.devops.app.eventhandler.payload.GitlabUserPayload;
import io.choerodon.devops.domain.application.valueobject.DeployKey;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.RepositoryFile;
import io.choerodon.devops.domain.application.valueobject.Variable;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.feign.fallback.GitlabServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * gitlab服务 feign客户端
 * Created by Zenger on 2018/3/28.
 */
@FeignClient(value = "gitlab-service", fallback= GitlabServiceClientFallback.class)
public interface GitlabServiceClient {
    @GetMapping(value = "/v1/users/{userId}")
    ResponseEntity<UserDO> queryUserByUserId(
            @PathVariable("userId") Integer userId);

    @GetMapping(value = "/v1/users/{username}/details")
    ResponseEntity<UserDO> queryUserByUserName(
            @PathVariable("username") String username);

    @GetMapping(value = "/v1/groups/{groupId}/members/{userId}")
    ResponseEntity<MemberDTO> getUserMemberByUserId(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId);

    @DeleteMapping(value = "/v1/groups/{groupId}/members/{userId}")
    ResponseEntity deleteMember(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId);

    @PostMapping(value = "/v1/groups/{groupId}/members")
    ResponseEntity<MemberDTO> insertMember(
            @PathVariable("groupId") Integer groupId,
            @RequestBody @Valid RequestMemberDO member);

    @PutMapping(value = "/v1/groups/{groupId}/members")
    ResponseEntity<MemberDTO> updateMember(
            @PathVariable("groupId") Integer groupId,
            @RequestBody @Valid RequestMemberDO member);

    @PostMapping(value = "/v1/users")
    ResponseEntity<UserDO> createGitLabUser(@RequestParam("password") String password,
                                            @RequestParam(value = "projectsLimit", required = false) Integer projectsLimit,
                                            @RequestBody GitlabUserPayload gitlabUserPayload);

    @PutMapping("/v1/users/{userId}")
    ResponseEntity<UserDO> updateGitLabUser(@PathVariable("userId") Integer userId,
                                            @RequestParam(value = "projectsLimit", required = false) Integer projectsLimit,
                                            @RequestBody GitlabUserPayload gitlabUserPayload);


    @PutMapping("/v1/projects/{projectId}")
    ResponseEntity<GitlabProjectDO> updateProject(@PathVariable("projectId") Integer projectId,
                                                  @RequestParam("userId") Integer userId);

    @PostMapping("/v1/projects")
    ResponseEntity<GitlabProjectDO> createProject(@RequestParam("groupId") Integer groupId,
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
    ResponseEntity<List<DeployKey>> getDeploykeys(@RequestParam("projectId") Integer projectId,
                                                  @RequestParam("userId") Integer userId);


    @PostMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<Map<String, Object>> addVariable(@PathVariable("projectId") Integer projectId,
                                                    @RequestParam("key") String key,
                                                    @RequestParam("value") String value,
                                                    @RequestParam("protecteds") Boolean protecteds,
                                                    @RequestParam("userId") Integer userId);

    @PutMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<List<Map<String, Object>>> batchAddVariable(@PathVariable("projectId") Integer projectId,
                                                               @RequestParam("userId") Integer userId,
                                                               @RequestBody @Valid List<VariableDTO> variableDTOS);

    @DeleteMapping(value = "/v1/projects/{projectId}")
    ResponseEntity deleteProject(@PathVariable("projectId") Integer projectId,
                                 @RequestParam("userId") Integer userId);

    @DeleteMapping(value = "/v1/projects/{groupName}/{projectName}")
    ResponseEntity deleteProjectByProjectName(@PathVariable("groupName") String groupName,
                                              @PathVariable("projectName") String projectName,
                                              @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{project_id}")
    ResponseEntity<GitlabProjectDO> getProjectById(@PathVariable("project_id") Integer projectId);

    @GetMapping(value = "/v1/projects/queryByName")
    ResponseEntity<GitlabProjectDO> getProjectByName(@RequestParam("userId") Integer userId,
                                                     @RequestParam("groupName") String groupName,
                                                     @RequestParam("projectName") String projectName);


    @GetMapping(value = "/v1/projects/{projectId}/variable")
    ResponseEntity<List<Variable>> getVariable(@PathVariable("projectId") Integer projectId,
                                               @RequestParam("userId") Integer userId);


    @PostMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<ImpersonationTokenDO> create(@PathVariable("userId") Integer userId);

    @PostMapping(value = "/v1/groups")
    ResponseEntity<GroupDO> createGroup(
            @RequestBody @Valid GroupDO group,
            @RequestParam("userId") Integer userId
    );

    @GetMapping(value = "/v1/groups/{groupId}/projects/event")
    ResponseEntity<List<GitlabProjectDO>> listProjects(@PathVariable("groupId") Integer groupId,
                                                       @RequestParam(value = "userId", required = false) Integer userId);

    @PostMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<ImpersonationTokenDO> createToken(@PathVariable("userId") Integer userId);

    @GetMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<List<ImpersonationTokenDO>> listTokenByUserId(@PathVariable("userId") Integer userId);

    @GetMapping(value = "/v1/groups/{groupName}")
    ResponseEntity<GroupDO> queryGroupByName(@PathVariable("groupName") String groupName,
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
    ResponseEntity<CompareResultsE> getCompareResults(@PathVariable("projectId") Integer projectId,
                                                      @RequestParam("from") String from,
                                                      @RequestParam("to") String to);

    @PostMapping(value = "/v1/projects/{projectId}/protected_branches")
    ResponseEntity<Map<String, Object>> createProtectedBranches(@PathVariable("projectId") Integer projectId,
                                                                @RequestParam("name") String name,
                                                                @RequestParam("mergeAccessLevel") String mergeAccessLevel,
                                                                @RequestParam("pushAccessLevel") String pushAccessLevel,
                                                                @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines")
    ResponseEntity<List<PipelineDO>> listPipeline(@PathVariable("projectId") Integer projectId,
                                                  @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/page")
    ResponseEntity<List<PipelineDO>> listPipelines(@PathVariable("projectId") Integer projectId,
                                                   @RequestParam("page") Integer page,
                                                   @RequestParam("size") Integer size,
                                                   @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}")
    ResponseEntity<PipelineDO> getPipeline(@PathVariable("projectId") Integer projectId,
                                           @PathVariable("pipelineId") Integer pipelineId,
                                           @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits")
    ResponseEntity<CommitDO> getCommit(@PathVariable("projectId") Integer projectId,
                                       @RequestParam("sha") String sha,
                                       @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits/project")
    ResponseEntity<List<CommitDO>> listCommits(@PathVariable("projectId") Integer projectId,
                                               @RequestParam("page") Integer page,
                                               @RequestParam("size") Integer size,
                                               @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits/statuse")
    ResponseEntity<List<CommitStatuseDO>> getCommitStatus(@PathVariable("projectId") Integer projectId,
                                                          @RequestParam("sha") String sha,
                                                          @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits/branch")
    ResponseEntity<List<CommitDO>> getCommits(@PathVariable("projectId") Integer projectId,
                                              @RequestParam("branchName") String branchName,
                                              @RequestParam("since") String since);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/jobs")
    ResponseEntity<List<JobDO>> listJobs(@PathVariable("projectId") Integer projectId,
                                         @PathVariable("pipelineId") Integer pipelineId,
                                         @RequestParam("userId") Integer userId);

    @PutMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}")
    ResponseEntity updateMergeRequest(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer merRequestId,
            @RequestParam(value = "userId", required = false) Integer userId);

    @GetMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}")
    ResponseEntity<MergeRequestDO> getMergeRequest(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer mergeRequestId,
            @RequestParam(value = "userId", required = false) Integer userId);

    @GetMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}/commit")
    ResponseEntity<List<CommitDO>> listCommits(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer mergeRequestId,
            @RequestParam(value = "userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/merge_requests")
    ResponseEntity<List<MergeRequestDO>> getMergeRequestList(@PathVariable("projectId") Integer projectId);

    @GetMapping("/v1/projects/{projectId}/repository/branches")
    ResponseEntity<List<BranchDO>> listBranches(@PathVariable("projectId") Integer projectId,
                                                @RequestParam(value = "userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/retry")
    ResponseEntity<PipelineDO> retry(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("pipelineId") Integer pipelineId,
            @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/cancel")
    ResponseEntity<PipelineDO> cancel(
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
    ResponseEntity<MergeRequestDO> createMergeRequest(
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
    ResponseEntity<MergeRequestDO> acceptMergeRequest(
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
    ResponseEntity<List<TagDO>> getTags(
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
    ResponseEntity<TagDO> createTag(
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
    ResponseEntity<TagDO> updateTagRelease(
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
    ResponseEntity<BranchDO> getBranch(
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
    ResponseEntity<BranchDO> createBranch(
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
    ResponseEntity<List<TagDO>> getPageTags(@PathVariable("projectId") Integer projectId,
                                            @RequestParam("page") int page,
                                            @RequestParam("perPage") int perPage,
                                            @RequestParam("userId") Integer userId);

    @PutMapping("/v1/users/{userId}/is_enabled")
    ResponseEntity enabledUserByUserId(@PathVariable("userId") Integer userId);

    @PutMapping("/v1/users/{userId}/dis_enabled")
    ResponseEntity disEnabledUserByUserId(@PathVariable("userId") Integer userId);

    @PostMapping("/v1/hook")
    ResponseEntity<ProjectHook> createProjectHook(
            @RequestParam("projectId") Integer projectId,
            @RequestParam("userId") Integer userId,
            @RequestBody ProjectHook projectHook);

    @PutMapping("/v1/hook")
    ResponseEntity<ProjectHook> updateProjectHook(
            @RequestParam("projectId") Integer projectId,
            @RequestParam("hookId") Integer hookId,
            @RequestParam("userId") Integer userId);

    @GetMapping("/v1/hook")
    ResponseEntity<List<ProjectHook>> getProjectHook(
            @RequestParam("projectId") Integer projectId,
            @RequestParam("userId") Integer userId);

    @PutMapping("/v1/groups/{groupId}")
    ResponseEntity updateGroup(@PathVariable("groupId") Integer groupId,
                               @RequestParam("userId") Integer userId,
                               @RequestBody @Valid GroupDO group);

    @PostMapping("/v1/projects/{projectId}/members")
    ResponseEntity addMemberIntoProject(@PathVariable("projectId") Integer projectId,
                                        @RequestBody MemberVO memberDTO);

    @PutMapping("/v1/projects/{projectId}/members")
    ResponseEntity updateMemberIntoProject(@PathVariable("projectId") Integer projectId,
                                           @RequestBody List<MemberVO> list);

    @GetMapping("/v1/projects/{projectId}/members/{userId}")
    ResponseEntity<MemberDTO> getProjectMember(@PathVariable("projectId") Integer projectId,
                                               @PathVariable("userId") Integer userId);

    @DeleteMapping("/v1/projects/{projectId}/members/{userId}")
    ResponseEntity removeMemberFromProject(@PathVariable("projectId") Integer projectId,
                                           @PathVariable("userId") Integer userId);

    @GetMapping("/v1/projects/{project_id}/members/list")
    ResponseEntity<List<MemberDTO>> getAllMemberByProjectId(@PathVariable(value = "project_id") Integer projectId);

    @GetMapping("/v1/projects/{user_id}/projects")
    ResponseEntity<List<GitlabProjectDO>> getProjectsByUserId(@PathVariable(value = "user_id") Integer id);

    @GetMapping("/v1/users/email/check")
    ResponseEntity<Boolean> checkEmailIsExist(@RequestParam(value = "email") String email);
}
