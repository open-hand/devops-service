package io.choerodon.devops.infra.feign;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.domain.application.event.GitlabUserEvent;
import io.choerodon.devops.infra.dataobject.gitlab.*;
import io.choerodon.devops.infra.feign.fallback.GitlabServiceClientFallback;

/**
 * gitlab服务 feign客户端
 * Created by Zenger on 2018/3/28.
 */
@FeignClient(value = "gitlab-service", fallback = GitlabServiceClientFallback.class)
public interface GitlabServiceClient {

    @GetMapping(value = "/v1/users/{username}/details")
    ResponseEntity<UserDO> queryUserByUsername(
            @PathVariable("username") String username);

    @GetMapping(value = "/v1/groups/{groupId}/members/{userId}")
    ResponseEntity<MemberDO> getUserMemberByUserId(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId);

    @DeleteMapping(value = "/v1/groups/{groupId}/members/{userId}")
    ResponseEntity deleteMember(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId);

    @PostMapping(value = "/v1/groups/{groupId}/members")
    ResponseEntity<MemberDO> insertMember(
            @PathVariable("groupId") Integer groupId,
            @RequestBody @Valid RequestMemberDO member);

    @PutMapping(value = "/v1/groups/{groupId}/members")
    ResponseEntity<MemberDO> updateMember(
            @PathVariable("groupId") Integer groupId,
            @RequestBody @Valid RequestMemberDO member);

    @PostMapping(value = "/v1/users")
    ResponseEntity<UserDO> createGitLabUser(@RequestParam("password") String password,
                                            @RequestParam(value = "projectsLimit", required = false) Integer projectsLimit,
                                            @RequestBody GitlabUserEvent gitlabUserEvent);

    @PutMapping("/v1/users/{username}")
    ResponseEntity<UserDO> updateGitLabUser(@PathVariable("username") String username,
                                            @RequestParam(value = "projectsLimit", required = false) Integer projectsLimit,
                                            @RequestBody GitlabUserEvent gitlabUserEvent);

    @DeleteMapping(value = "/v1/users/{username}")
    ResponseEntity deleteGitLabUser(@PathVariable("username") String username);

    @PostMapping(value = "/v1/projects/name/event")
    ResponseEntity<GitlabProjectDO> createProject(@RequestParam("groupId") Integer groupId,
                                                  @RequestParam("projectName") String projectName,
                                                  @RequestParam("userName") String userName);

    @PutMapping("/v1/projects/{projectId}")
    ResponseEntity<GitlabProjectDO> updateProject(@PathVariable("projectId") Integer projectId,
                                                  @RequestParam("userName") String userName);

    @PostMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<Map<String, Object>> addVariable(@PathVariable("projectId") Integer projectId,
                                                    @RequestParam("key") String key,
                                                    @RequestParam("value") String value,
                                                    @RequestParam("protecteds") Boolean protecteds);

    @PostMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<Map<String, Object>> addVariable(@PathVariable("projectId") Integer projectId,
                                                    @RequestParam("key") String key,
                                                    @RequestParam("value") String value,
                                                    @RequestParam("protecteds") Boolean protecteds,
                                                    @RequestParam("userName") String userName);

    @DeleteMapping(value = "/v1/projects/{projectId}")
    ResponseEntity deleteProject(@PathVariable("projectId") Integer projectId,
                                 @RequestParam("userName") String userName);

    @GetMapping("/v1/projects/{projectId}/jobs/{jobId}")
    ResponseEntity<JobDO> getJob(@PathVariable("projectId") Integer projectId, @PathVariable("jobId") Integer jobId);

    @PostMapping(value = "/v1/projects/{projectId}/protected_branches")
    ResponseEntity<Map<String, Object>> insertProtectedBranches(
            @PathVariable("projectId") Integer projectId,
            @RequestParam("name") String name,
            @RequestParam("merge_access_level") String mergeAccessLevel,
            @RequestParam("push_access_level") String pushAccessLevel,
            @RequestParam("userName") String userName);


    @PostMapping(value = "/v1/users/{username}/impersonation_tokens")
    ResponseEntity<ImpersonationTokenDO> create(@PathVariable("username") String username);

    @GetMapping(value = "/v1/users/{username}/impersonation_tokens")
    ResponseEntity<List<ImpersonationTokenDO>> selectAll(@PathVariable("username") String username);

    @PostMapping(value = "/v1/groups")
    ResponseEntity<GroupDO> createGroup(
            @RequestBody @Valid GroupDO group,
            @RequestParam("userName") String userName
    );

    @PostMapping(value = "/v1/groups")
    ResponseEntity<GroupDO> createGroup(@RequestBody GroupDO groupDO);

    @GetMapping(value = "/v1/groups/{groupId}/projects/event")
    ResponseEntity<List<GitlabProjectDO>> listProjects(@PathVariable("groupId") Integer groupId,
                                                       @RequestParam(value = "userName", required = false) String userName);

    @PostMapping(value = "/v1/users/{username}/impersonation_tokens")
    ResponseEntity<ImpersonationTokenDO> createToken(@PathVariable("username") String username);

    @GetMapping(value = "/v1/users/{username}/impersonation_tokens")
    ResponseEntity<List<ImpersonationTokenDO>> listTokenByUserName(@PathVariable("username") String username);


    @PostMapping(value = "/v1/hook")
    ResponseEntity<ProjectHookDO> addHookByProjectHook(@RequestParam("projectId") Integer projectId, @RequestBody ProjectHookDO projectHook,
                                                       @RequestParam("userName") String userName);

    @GetMapping(value = "/v1/groups/{groupName}")
    ResponseEntity<GroupDO> queryGroupByName(@PathVariable("groupName") String groupName);

    @PostMapping(value = "/v1/projects/{projectId}/repository/file")
    ResponseEntity<Boolean> createFile(@PathVariable("projectId") Integer projectId,
                                       @RequestParam("userName") String userName);

    @PostMapping(value = "/v1/projects/{projectId}/protected_branches")
    ResponseEntity<Map<String, Object>> createProtectedBranches(@PathVariable("projectId") Integer projectId,
                                                                @RequestParam("name") String name,
                                                                @RequestParam("mergeAccessLevel") String mergeAccessLevel,
                                                                @RequestParam("pushAccessLevel") String pushAccessLevel,
                                                                @RequestParam("userName") String userName);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines")
    ResponseEntity<List<PipelineDO>> listPipeline(@PathVariable("projectId") Integer projectId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/page")
    ResponseEntity<List<PipelineDO>> listPipelines(@PathVariable("projectId") Integer projectId,
                                                   @RequestParam("page") Integer page, @RequestParam("size") Integer size);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}")
    ResponseEntity<PipelineDO> getPipeline(@PathVariable("projectId") Integer projectId,
                                           @PathVariable("pipelineId") Integer pipelineId,
                                           @RequestParam("userName") String userName);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits")
    ResponseEntity<CommitDO> getCommit(@PathVariable("projectId") Integer projectId,
                                       @RequestParam("sha") String sha,
                                       @RequestParam("userName") String userName);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/jobs")
    ResponseEntity<List<JobDO>> listJobs(@PathVariable("projectId") Integer projectId,
                                         @PathVariable("pipelineId") Integer pipelineId,
                                         @RequestParam("userName") String userName);

    @PutMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}")
    ResponseEntity updateMergeRequest(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer merRequestId,
            @RequestParam(value = "username", required = false) String username);

    @GetMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}")
    ResponseEntity<MergeRequestDO> getMergeRequest(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer mergeRequestId,
            @RequestParam(value = "username", required = false) String username);

    @GetMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}/commit")
    ResponseEntity<List<CommitDO>> listCommits(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("mergeRequestId") Integer mergeRequestId);

    @GetMapping("/v1/projects/{projectId}/repository/branches")
    public ResponseEntity<List<BranchDO>> listBranches(@PathVariable("projectId") Integer projectId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/retry")
    public ResponseEntity<PipelineDO> retry(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("pipelineId") Integer pipelineId,
            @RequestParam("userName") String userName);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/cancel")
    public ResponseEntity<PipelineDO> cancel(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("pipelineId") Integer pipelineId,
            @RequestParam("userName") String userName);


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
            @RequestParam(value = "username", required = false) String username);

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
            @RequestParam(value = "username", required = false) String username);

    /**
     * 获取tag列表
     *
     * @param projectId 工程ID
     * @return tag列表
     */
    @GetMapping("/v1/projects/{projectId}/repository/tags")
    ResponseEntity<List<TagDO>> getTags(
            @PathVariable("projectId") Integer projectId,
            @RequestParam(value = "username", required = false) String username);

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
            @RequestParam("username") String username);

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
            @RequestParam("username") String username);

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
            @RequestParam("source") String source);

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
                                            @RequestParam("perPage") int perPage);

    @PutMapping("/v1/users/{username}/is_enabled")
    ResponseEntity enabledUserByUsername(@PathVariable("username") String username);

    @PutMapping("/v1/users/{username}/dis_enabled")
    ResponseEntity disEnabledUserByUsername(@PathVariable("username") String username);
}
