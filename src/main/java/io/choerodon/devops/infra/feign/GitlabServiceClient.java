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

    @GetMapping(value = "/v1/users/{userId}")
    ResponseEntity<UserDO> queryUserByUserId(
            @PathVariable("userId") Integer userId);

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

    @PutMapping("/v1/users/{userId}")
    ResponseEntity<UserDO> updateGitLabUser(@PathVariable("userId") Integer userId,
                                            @RequestParam(value = "projectsLimit", required = false) Integer projectsLimit,
                                            @RequestBody GitlabUserEvent gitlabUserEvent);


    @PutMapping("/v1/projects/{projectId}")
    ResponseEntity<GitlabProjectDO> updateProject(@PathVariable("projectId") Integer projectId,
                                                  @RequestParam("userId") Integer userId);

    @PostMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<Map<String, Object>> addVariable(@PathVariable("projectId") Integer projectId,
                                                    @RequestParam("key") String key,
                                                    @RequestParam("value") String value,
                                                    @RequestParam("protecteds") Boolean protecteds,
                                                    @RequestParam("userId") Integer userId);

    @DeleteMapping(value = "/v1/projects/{projectId}")
    ResponseEntity deleteProject(@PathVariable("projectId") Integer projectId,
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
    ResponseEntity<GroupDO> queryGroupByName(@PathVariable("groupName") String groupName,@RequestParam(value = "userId") Integer userId);

    @PostMapping(value = "/v1/projects/{projectId}/repository/file")
    ResponseEntity<Boolean> createFile(@PathVariable("projectId") Integer projectId,
                                       @RequestParam("userId") Integer userId);

    @PostMapping(value = "/v1/projects/{projectId}/protected_branches")
    ResponseEntity<Map<String, Object>> createProtectedBranches(@PathVariable("projectId") Integer projectId,
                                                                @RequestParam("name") String name,
                                                                @RequestParam("mergeAccessLevel") String mergeAccessLevel,
                                                                @RequestParam("pushAccessLevel") String pushAccessLevel,
                                                                @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines")
    ResponseEntity<List<PipelineDO>> listPipeline(@PathVariable("projectId") Integer projectId,@RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/page")
    ResponseEntity<List<PipelineDO>> listPipelines(@PathVariable("projectId") Integer projectId,
                                                   @RequestParam("page") Integer page, @RequestParam("size") Integer size,@RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}")
    ResponseEntity<PipelineDO> getPipeline(@PathVariable("projectId") Integer projectId,
                                           @PathVariable("pipelineId") Integer pipelineId,
                                           @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits")
    ResponseEntity<CommitDO> getCommit(@PathVariable("projectId") Integer projectId,
                                       @RequestParam("sha") String sha,
                                       @RequestParam("userId") Integer userId);

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
            @RequestParam("userId") Integer userId);

    @DeleteMapping("/v1/projects/{projectId}/merge_requests/{mergeRequestId}")
    ResponseEntity deleteMergeRequest(@PathVariable("projectId") Integer projectId,
                                      @PathVariable("mergeRequestId") Integer mergeRequestId,
                                      @RequestParam("userId") Integer userId);

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
    ResponseEntity enabledUserByUsername(@PathVariable("userId") Integer userId);

    @PutMapping("/v1/users/{userId}/dis_enabled")
    ResponseEntity disEnabledUserByUsername(@PathVariable("userId") Integer userId);
}
