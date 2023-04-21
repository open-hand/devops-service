package io.choerodon.devops.infra.feign;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.CiVariableVO;
import io.choerodon.devops.api.vo.FileCreationVO;
import io.choerodon.devops.infra.dto.RepositoryFileDTO;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.dto.gitlab.ci.Pipeline;
import io.choerodon.devops.infra.feign.fallback.GitlabServiceClientFallback;

/**
 * gitlab服务 feign客户端
 * Created by Zenger on 2018/3/28.
 */
@FeignClient(value = "gitlab-service", fallback = GitlabServiceClientFallback.class)
public interface GitlabServiceClient {
    @GetMapping(value = "/v1/users/{userId}")
    ResponseEntity<GitLabUserDTO> queryUserById(
            @PathVariable("userId") Integer userId);

    @GetMapping(value = "/v1/users/{username}/details")
    ResponseEntity<GitLabUserDTO> queryUserByUserName(
            @PathVariable("username") String username);

    @GetMapping(value = "/v1/users/currentUser")
    ResponseEntity<GitLabUserDTO> queryAdminUser();

    @GetMapping(value = "/v1/groups/{groupId}/members/{userId}")
    ResponseEntity<MemberDTO> queryGroupMember(
            @PathVariable("groupId") Integer groupId,
            @PathVariable("userId") Integer userId);

    @GetMapping(value = "/v1/groups/{groupId}/members")
    ResponseEntity<List<MemberDTO>> listGroupMember(
            @PathVariable("groupId") Integer groupId);

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
    ResponseEntity<GitLabUserDTO> createUser(@RequestParam(value = "projectsLimit", required = false) Integer projectsLimit,
                                             @RequestBody GitlabTransferDTO gitlabTransferDTO);

    @PutMapping("/v1/users/{userId}")
    ResponseEntity<GitLabUserDTO> updateGitLabUser(@PathVariable("userId") Integer userId,
                                                   @RequestParam(value = "projectsLimit", required = false) Integer projectsLimit,
                                                   @RequestBody GitlabUserReqDTO userReqDTO);

    /**
     * 根据用户Id更新用户密码
     *
     * @param userId 用户Id
     * @param user   用户密码信息F
     */
    @PutMapping(value = "/v1/users/{userId}/password")
    ResponseEntity<GitLabUserDTO> updateUserPasswordByUserId(
            @PathVariable("userId") Integer userId,
            @RequestBody GitlabUserWithPasswordDTO user);


    @PostMapping("/v1/projects")
    ResponseEntity<GitlabProjectDTO> createProject(@RequestParam("groupId") Integer groupId,
                                                   @RequestParam("projectName") String projectName,
                                                   @RequestParam("userId") Integer userId,
                                                   @RequestParam("visibility") boolean visibility);

    @PostMapping("/v1/projects/deploy_key")
    ResponseEntity createDeploykey(@RequestParam("projectId") Integer projectId,
                                   @RequestBody GitlabTransferDTO gitlabTransferDTO,
                                   @RequestParam("canPush") boolean canPush,
                                   @RequestParam("userId") Integer userId);

    @GetMapping("/v1/projects/deploy_key")
    ResponseEntity<List<DeployKeyDTO>> listDeploykey(@RequestParam("projectId") Integer projectId,
                                                     @RequestParam("userId") Integer userId);

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
                                                        @RequestParam("projectName") String projectName,
                                                        @RequestParam(value = "statistics", defaultValue = "false") Boolean statistics);


    @PostMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<ImpersonationTokenDTO> create(@PathVariable("userId") Integer userId);

    @PostMapping(value = "/v1/groups")
    ResponseEntity<GroupDTO> createGroup(
            @RequestBody @Valid GroupDTO group,
            @RequestParam("userId") Integer userId
    );

    @GetMapping(value = "/v1/groups/{groupId}/projects/event")
    ResponseEntity<List<GitlabProjectDTO>> listProjects(@PathVariable("groupId") Integer groupId,
                                                        @RequestParam(value = "userId", required = false) Integer userId,
                                                        @RequestParam(value = "page", required = false) Integer page,
                                                        @RequestParam(value = "perPage", required = false) Integer perPage);

    @PostMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<ImpersonationTokenDTO> createProjectToken(@PathVariable("userId") Integer userId,
                                                             @RequestParam(value = "tokenName", required = false) String tokenName,
                                                             @RequestParam(value = "date", required = false) Date date);

    @DeleteMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<Void> revokeImpersonationToken(@PathVariable("userId") Integer userId,
                                                  @RequestParam(value = "tokenId") Integer tokenId);

    @GetMapping(value = "/v1/users/{userId}/impersonation_tokens")
    ResponseEntity<List<ImpersonationTokenDTO>> listProjectToken(@PathVariable("userId") Integer userId);

    /**
     * 根据组的path查询组
     *
     * @param groupName 组的path
     * @param userId    用户id
     * @return 组
     */
    @GetMapping(value = "/v1/groups/{groupName}")
    ResponseEntity<GroupDTO> queryGroupByName(@PathVariable("groupName") String groupName,
                                              @RequestParam(value = "userId") Integer userId);

    @PostMapping(value = "/v1/projects/{projectId}/repository/file")
    ResponseEntity<RepositoryFileDTO> createFile(@PathVariable("projectId") Integer projectId,
                                                 @RequestBody FileCreationVO fileCreationVO,
                                                 @RequestParam(value = "gitlabUrl") String gitlabUrl,
                                                 @RequestParam(value = "authType") String authType,
                                                 @RequestParam(value = "accessToken") String accessToken,
                                                 @RequestParam(value = "username") String username,
                                                 @RequestParam(value = "password") String password);

    @PutMapping(value = "/v1/projects/{projectId}/repository/file")
    ResponseEntity<RepositoryFileDTO> updateFile(@PathVariable("projectId") Integer projectId,
                                                 @RequestBody FileCreationVO fileCreationVO,
                                                 @RequestParam(value = "gitlabUrl") String gitlabUrl,
                                                 @RequestParam(value = "authType") String authType,
                                                 @RequestParam(value = "accessToken") String accessToken,
                                                 @RequestParam(value = "username") String username,
                                                 @RequestParam(value = "password") String password);

    @DeleteMapping(value = "/v1/projects/{projectId}/repository/file")
    ResponseEntity deleteFile(@PathVariable("projectId") Integer projectId,
                              @RequestBody FileCreationVO fileCreationVO,
                              @RequestParam(value = "gitlabUrl") String gitlabUrl,
                              @RequestParam(value = "authType") String authType,
                              @RequestParam(value = "accessToken") String accessToken,
                              @RequestParam(value = "username") String username,
                              @RequestParam(value = "password") String password);

    @GetMapping(value = "/v1/projects/{projectId}/repository/{commit}/file")
    ResponseEntity<RepositoryFileDTO> getFile(@PathVariable("projectId") Integer projectId,
                                              @PathVariable("commit") String commit,
                                              @RequestParam(value = "file_path") String filePath);

    @GetMapping(value = "/v1/external_projects/{projectId}/repository/{commit}/file")
    ResponseEntity<RepositoryFileDTO> getExternalFile(@PathVariable("projectId") Integer projectId,
                                                      @PathVariable("commit") String commit,
                                                      @RequestParam(value = "file_path") String filePath,
                                                      @RequestParam(value = "gitlabUrl") String gitlabUrl,
                                                      @RequestParam(value = "authType") String authType,
                                                      @RequestParam(value = "accessToken") String accessToken,
                                                      @RequestParam(value = "username") String username,
                                                      @RequestParam(value = "password") String password);

    @PostMapping(value = "/v1/projects/{projectId}/repository/file/diffs")
    ResponseEntity<CompareResultDTO> queryCompareResult(@PathVariable("projectId") Integer projectId,
                                                        @RequestBody GitlabTransferDTO gitlabTransferDTO);

    @PostMapping(value = "/v1/projects/{projectId}/protected_branches")
    ResponseEntity<Map<String, Object>> createProtectedBranch(@PathVariable("projectId") Integer projectId,
                                                              @RequestBody GitlabTransferDTO gitlabTransferDTO,
                                                              @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines")
    ResponseEntity<List<GitlabPipelineDTO>> listPipeline(@PathVariable("projectId") Integer projectId,
                                                         @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/page")
    ResponseEntity<List<GitlabPipelineDTO>> pagePipeline(@PathVariable("projectId") Integer projectId,
                                                         @RequestParam("page") Integer page,
                                                         @RequestParam("size") Integer size,
                                                         @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}")
    ResponseEntity<GitlabPipelineDTO> queryPipeline(@PathVariable("projectId") Integer projectId,
                                                    @PathVariable("pipelineId") Integer pipelineId,
                                                    @RequestParam("userId") Integer userId,
                                                    @RequestParam(value = "gitlabUrl") String gitlabUrl,
                                                    @RequestParam(value = "authType") String authType,
                                                    @RequestParam(value = "accessToken") String accessToken,
                                                    @RequestParam(value = "username") String username,
                                                    @RequestParam(value = "password") String password);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits")
    ResponseEntity<CommitDTO> queryCommit(@PathVariable("projectId") Integer projectId,
                                          @RequestParam("sha") String sha,
                                          @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits/project")
    ResponseEntity<List<CommitDTO>> listCommits(@PathVariable("projectId") Integer projectId,
                                                @RequestParam("page") Integer page,
                                                @RequestParam("size") Integer size,
                                                @RequestParam("userId") Integer userId);

    @GetMapping(value = "/v1/external_projects/{projectId}/repository/commits/project")
    ResponseEntity<List<CommitDTO>> listExternalCommits(@PathVariable("projectId") Integer projectId,
                                                        @RequestParam("page") Integer page,
                                                        @RequestParam("size") Integer size,
                                                        @RequestParam(value = "gitlabUrl") String gitlabUrl,
                                                        @RequestParam(value = "authType") String authType,
                                                        @RequestParam(value = "accessToken") String accessToken,
                                                        @RequestParam(value = "username") String username,
                                                        @RequestParam(value = "password") String password);

    @GetMapping(value = "/v1/projects/{projectId}/repository/commits/statuse")
    ResponseEntity<List<CommitStatusDTO>> listCommitStatus(@PathVariable("projectId") Integer projectId,
                                                           @RequestParam("sha") String sha,
                                                           @RequestParam("userId") Integer userId);

    @PostMapping(value = "/v1/projects/{projectId}/repository/commits/branch")
    ResponseEntity<List<CommitDTO>> getCommits(@PathVariable("projectId") Integer projectId,
                                               @RequestBody GitlabTransferDTO gitlabTransferDTO);


    @GetMapping(value = "/v1/projects/{projectId}/repository/commits/branch/{ref}")
    ResponseEntity<List<CommitDTO>> getCommitsByRef(@PathVariable("projectId") Integer projectId,
                                                    @PathVariable(value = "ref") String ref);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/jobs")
    ResponseEntity<List<JobDTO>> listJobs(@PathVariable("projectId") Integer projectId,
                                          @PathVariable("pipelineId") Integer pipelineId,
                                          @RequestParam("userId") Integer userId,
                                          @RequestParam(value = "gitlabUrl") String gitlabUrl,
                                          @RequestParam(value = "authType") String authType,
                                          @RequestParam(value = "accessToken") String accessToken,
                                          @RequestParam(value = "username") String username,
                                          @RequestParam(value = "password") String password);

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
                                               @RequestParam(value = "userId") Integer userId,
                                               @RequestParam(value = "gitlabUrl") String gitlabUrl,
                                               @RequestParam(value = "authType") String authType,
                                               @RequestParam(value = "accessToken") String accessToken,
                                               @RequestParam(value = "username") String username,
                                               @RequestParam(value = "password") String password);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/retry")
    ResponseEntity<Pipeline> retryPipeline(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("pipelineId") Integer pipelineId,
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

    @GetMapping(value = "/v1/projects/{projectId}/pipelines/{pipelineId}/cancel")
    ResponseEntity<Pipeline> cancelPipeline(
            @PathVariable("projectId") Integer projectId,
            @PathVariable("pipelineId") Integer pipelineId,
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);


    /**
     * 创建merge请求
     *
     * @param projectId 工程ID
     * @return 创建的merge请求
     */
    @PostMapping("/v1/projects/{projectId}/merge_requests")
    ResponseEntity<MergeRequestDTO> createMergeRequest(
            @PathVariable("projectId") Integer projectId,
            @RequestBody GitlabTransferDTO gitlabTransferDTO,
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
            @RequestBody String mergeCommitMessage,
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
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

    /**
     * 创建tag
     *
     * @param projectId 工程ID
     * @return 创建的tag
     */
    @PostMapping("/v1/projects/{projectId}/repository/tags")
    ResponseEntity<TagDTO> createTag(
            @PathVariable("projectId") Integer projectId,
            @RequestBody GitlabTransferDTO gitlabTransferDTO,
            @RequestParam("userId") Integer userId);

    @PostMapping("/v1/projects/{project_id}/releases")
    ResponseEntity<Release> createRelease(
            @PathVariable(value = "project_id") Integer projectId,
            @RequestParam(required = false) Integer userId,
            @RequestBody(required = false) ReleaseParams release);

    @PutMapping("/v1/projects/{project_id}/releases")
    ResponseEntity<Release> updateRelease(
            @PathVariable(value = "project_id") Integer projectId,
            @RequestParam(required = false) Integer userId,
            @RequestBody(required = false) ReleaseParams release);

    @PutMapping("/v1/projects/{project_id}/releases/{tag_name}")
    ResponseEntity<Release> queryRelease(
            @PathVariable(value = "project_id") Integer projectId,
            @RequestParam(required = false) Integer userId,
            @PathVariable(value = "tag_name") String tagName);

    /**
     * 更新 tag
     *
     * @param projectId 项目id
     * @return Tag
     */
    @PutMapping("/v1/projects/{projectId}/repository/tags")
    ResponseEntity<TagDTO> updateTag(
            @PathVariable("projectId") Integer projectId,
            @RequestBody GitlabTransferDTO gitlabTransferDTO,
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
            @RequestBody String name,
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
            @RequestBody String branchName,
            @RequestParam("userId") Integer userId);


    /**
     * 根据分支名查询分支
     *
     * @param projectId  工程ID
     * @param branchName 分支名
     * @return 不含任何消息体的ResponseEntity
     */
    @GetMapping("/v1/projects/{projectId}/repository/branches/query")
    ResponseEntity<BranchDTO> queryBranch(
            @PathVariable("projectId") Integer projectId,
            @RequestParam("branchName") String branchName);

    //todo 如果name里面有&字符，&后面的部分会被丢弃

    /**
     * 创建新分支的接口
     *
     * @param projectId 工程ID
     * @return 创建的分支
     */
    @PostMapping("/v1/projects/{projectId}/repository/branches")
    ResponseEntity<BranchDTO> createBranch(
            @PathVariable("projectId") Integer projectId,
            @RequestBody GitlabTransferDTO gitlabTransferDTO,
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

    @PostMapping("/v1/external_projects/hook")
    ResponseEntity<String> createExternalProjectHook(
            @RequestParam("projectId") Integer projectId,
            @RequestBody ProjectHookDTO projectHookDTO,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

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

    @GetMapping("/v1/projects/{projectId}/merge_requests/ids")
    ResponseEntity<List<Long>> listIds(
            @ApiParam(value = "gitlab项目id", required = true)
            @PathVariable Integer projectId);

    @DeleteMapping("/v1/projects/{projectId}/members/{userId}")
    ResponseEntity deleteProjectMember(@PathVariable("projectId") Integer projectId,
                                       @PathVariable("userId") Integer userId);

    @GetMapping("/v1/projects/{project_id}/members/list")
    ResponseEntity<List<MemberDTO>> listMemberByProject(@PathVariable(value = "project_id") Integer projectId);

    @GetMapping("/v1/projects/{user_id}/projects")
    ResponseEntity<List<GitlabProjectDTO>> listProjectByUser(@PathVariable(value = "user_id") Integer id);

    @GetMapping("/v1/users/email/check")
    ResponseEntity<Boolean> checkEmail(@RequestParam(value = "email") String email);

    @GetMapping("/v1/users/query_user/email")
    ResponseEntity<GitLabUserDTO> queryUserByEmail(@RequestParam(value = "email") String email);

    @GetMapping("/v1/confings/get_admin_token")
    ResponseEntity<String> getAdminToken();

    /**
     * 判断用户是否是admin
     *
     * @param userId gitlab用户id
     * @return true表示是
     */
    @GetMapping("/v1/users/{userId}/admin")
    ResponseEntity<Boolean> checkIsAdmin(@PathVariable("userId") Integer userId);

    /**
     * 为用户添加admin权限
     *
     * @param userId gitlab用户id
     * @return true表示加上了
     */
    @PutMapping("/v1/users/{userId}/admin")
    ResponseEntity<Boolean> assignAdmin(@PathVariable("userId") Integer userId);

    /**
     * 删除用户admin权限
     *
     * @param userId gitlab用户id
     * @return true表示删除了
     */
    @DeleteMapping("/v1/users/{userId}/admin")
    ResponseEntity<Boolean> deleteAdmin(@PathVariable("userId") Integer userId);


    @ApiOperation(value = "查出组下所有的AccessRequest")
    @GetMapping(value = "/v1/groups/{groupId}/access_requests")
    ResponseEntity<List<AccessRequestDTO>> listAccessRequestsOfGroup(
            @ApiParam("组id")
            @PathVariable("groupId") Integer groupId);

    /**
     * 这个接口不抛出关于GitlabApi的异常
     *
     * @param groupId 组id
     * @param userId  被拒绝的用户的id
     * @return OK
     */
    @ApiOperation(value = "拒绝组下某个人的AccessRequest请求")
    @DeleteMapping(value = "/v1/groups/{groupId}/access_requests")
    ResponseEntity denyAccessRequest(
            @ApiParam(value = "组id")
            @PathVariable("groupId") Integer groupId,
            @ApiParam(value = "被拒绝的用户id")
            @RequestParam("user_id") Integer userId);

    @PostMapping(value = "/v1/projects/{projectId}/repository/commits")
    @ApiOperation("创建commit，可以批量操作文件")
    ResponseEntity createCommit(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "projectId") Integer projectId,
            @ApiParam(value = "用户名", required = true)
            @RequestParam(value = "user_id") Integer userId,
            @ApiParam(value = "操作文件相关的信息")
            @RequestBody CommitPayloadDTO commitPayloadDTO);

    /**
     * Create a new pipeline
     *
     * @param projectId 项目id
     * @param ref       分支
     * @param variables
     * @return Pipeline
     */
    @ApiOperation(value = "Create a pipelines jobs ")
    @PostMapping("/v1/projects/{projectId}/pipelines")
    ResponseEntity<Pipeline> createPipeline(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "projectId") Integer projectId,
            @ApiParam(value = "userId")
            @RequestParam(value = "userId") Integer userId,
            @ApiParam(value = "分支")
            @RequestParam(value = "ref") String ref,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @RequestBody Map<String, String> variables);

    /**
     * 查询job执行日志
     */
    @GetMapping(value = "/v1/projects/{projectId}/jobs/{jobId}/trace")
    ResponseEntity<String> queryTrace(
            @PathVariable(value = "projectId") Integer projectId,
            @PathVariable(value = "jobId") Integer jobId,
            @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

    @ApiOperation(value = "重试job")
    @PutMapping(value = "/v1/projects/{projectId}/jobs/{jobId}/retry")
    ResponseEntity<JobDTO> retryJob(
            @PathVariable(value = "projectId") Integer projectId,
            @PathVariable(value = "jobId") Integer jobId,
            @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

    @ApiOperation(value = "执行 manul job")
    @PutMapping(value = "/v1/projects/{projectId}/jobs/{jobId}/play")
    ResponseEntity<JobDTO> playJob(
            @PathVariable(value = "projectId") Integer projectId,
            @PathVariable(value = "jobId") Integer jobId,
            @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

    /**
     * 列举出gitlab项目组的ci variable
     *
     * @param groupId 组id
     * @param userId  gitlab用户id
     * @return
     */
    @GetMapping(value = "/v1/groups/{groupId}/variable")
    ResponseEntity<List<CiVariableVO>> listProjectVariable(@PathVariable("groupId") Integer groupId,
                                                           @RequestParam("userId") Integer userId);

    @ApiOperation(value = "添加组ci环境变量")
    @PostMapping(value = "/v1/groups/{groupId}/variable")
    ResponseEntity<CiVariableVO> createGroupVariable(
            @ApiParam(value = "组ID", required = true)
            @PathVariable("groupId") Integer groupId,
            @ApiParam(value = "变量key&value", required = true)
            @RequestBody GitlabTransferDTO gitlabTransferDTO,
            @ApiParam(value = "变量是否保护", required = true)
            @RequestParam("protecteds") boolean protecteds,
            @ApiParam(value = "用户Id称")
            @RequestParam(required = false, name = "userId") Integer userId);

    /**
     * 批量增加/更新组ci环境变量
     *
     * @param groupId 组id
     * @param userId  用户id
     * @param list    变量列表
     * @return 变量列表
     */
    @ApiOperation(value = " 批量增加/更新项目ci环境变量")
    @PutMapping(value = "/v1/groups/{groupId}/variables")
    ResponseEntity<List<CiVariableVO>> batchSaveGroupVariable(
            @ApiParam(value = "组ID", required = true)
            @PathVariable("groupId") Integer groupId,
            @ApiParam(value = "用户ID", required = true)
            @RequestParam(value = "userId") Integer userId,
            @ApiParam(value = "variable信息", required = true)
            @RequestBody List<CiVariableVO> list);

    /**
     * 删除组中指定key的变量
     *
     * @param groupId 组id
     * @param userId  用户id
     * @param key     key
     * @return 204 code
     */
    @ApiOperation(value = "删除ci环境变量")
    @DeleteMapping(value = "/v1/groups/{groupId}/variables")
    ResponseEntity<Void> deleteVariable(
            @ApiParam(value = "组ID", required = true)
            @PathVariable("groupId") Integer groupId,
            @ApiParam(value = "用户ID", required = true)
            @RequestParam(value = "userId") Integer userId,
            @ApiParam(value = "variable key", required = true)
            @RequestParam(value = "key") String key);

    /**
     * 批量删除组中指定key的变量
     *
     * @param groupId 组id
     * @param userId  用户id
     * @param key     key
     * @return 204 code
     */
    @ApiOperation(value = "批量删除组中指定key的变量")
    @DeleteMapping(value = "/v1/groups/{groupId}/variables")
    ResponseEntity<Void> batchGroupDeleteVariable(
            @ApiParam(value = "组ID", required = true)
            @PathVariable("groupId") Integer groupId,
            @ApiParam(value = "用户ID", required = true)
            @RequestParam(value = "userId") Integer userId,
            @ApiParam(value = "variable keys", required = true)
            @RequestBody List<String> key);

    /**
     * 列举出gitlab项目的ci variable
     *
     * @param projectId gitlab项目id
     * @param userId    gitlab用户id
     * @return
     */
    @GetMapping(value = "/v1/projects/{projectId}/variable")
    ResponseEntity<List<CiVariableVO>> listAppServiceVariable(@PathVariable("projectId") Integer projectId,
                                                              @RequestParam("userId") Integer userId);


    @PostMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<CiVariableVO> addProjectVariable(@PathVariable("projectId") Integer projectId,
                                                    @RequestBody GitlabTransferDTO gitlabTransferDTO,
                                                    @RequestParam("protecteds") Boolean protecteds,
                                                    @RequestParam("userId") Integer userId);

    @PutMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<List<CiVariableVO>> batchSaveProjectVariable(@PathVariable("projectId") Integer projectId,
                                                                @RequestParam("userId") Integer userId,
                                                                @RequestBody List<CiVariableVO> ciVariableVOList);

    /**
     * 批量删除项目中指定key的变量
     *
     * @param projectId 项目id
     * @param userId    用户id
     * @param key       key
     * @return 204 code
     */
    @ApiOperation(value = "批量删除项目中指定key的变量")
    @DeleteMapping(value = "/v1/projects/{projectId}/variables")
    ResponseEntity<Void> batchProjectDeleteVariable(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "projectId") Integer projectId,
            @ApiParam(value = "用户ID", required = true)
            @RequestParam(value = "userId") Integer userId,
            @ApiParam(value = "variable keys", required = true)
            @RequestBody List<String> key);

    /**
     * 更新项目
     *
     * @param project 项目对象
     * @param userId  用户Id
     * @return {@link Project}
     */
    @ApiOperation(value = "更新项目")
    @PutMapping("/v1/projects")
    ResponseEntity<String> updateProject(
            @ApiParam(value = "用户Id", required = true)
            @RequestParam Integer userId,
            @ApiParam(value = "项目信息", required = true)
            @RequestBody Project project);

    @PutMapping("/v1/projects/{projectId}/name_and_path")
    ResponseEntity<Project> updateNameAndPath(
            @PathVariable Integer projectId,
            @ApiParam(value = "用户Id", required = true)
            @RequestParam Integer userId,
            @RequestParam String name);

    @ApiOperation(value = "查询MR下的note列表")
    @GetMapping("/v1/notes/on_merge_request")
    ResponseEntity<List<Note>> listByMergeRequestIid(
            @ApiParam(value = "项目Id", required = true)
            @RequestParam Integer projectId,
            @ApiParam(value = "MR Iid", required = true)
            @RequestParam Integer iid);

    @ApiOperation(value = "查询有权限的所有组")
    @PostMapping("/v1/groups/{userId}")
    ResponseEntity<List<GroupDTO>> listGroupsWithParam(@ApiParam(value = "userId")
                                                       @PathVariable(value = "userId") Integer userId,
                                                       @RequestParam(value = "owned", required = false) Boolean owned,
                                                       @RequestParam(value = "search", required = false) String search,
                                                       @RequestBody List<Integer> skipGroups);

    @ApiOperation(value = "获取项目列表")
    @GetMapping(value = "/v1/groups/{groupId}/projects")
    ResponseEntity<List<GitlabProjectDTO>> listProjects(
            @ApiParam(value = "组ID", required = true)
            @PathVariable(value = "groupId") Integer groupId,
            @ApiParam(value = "userId")
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "owned", required = false) Boolean owned,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "perPage", required = false) Integer perPage);

    @ApiParam(value = "迁移应用服务")
    @PutMapping(value = "/v1/projects/{projectId}/transfer")
    ResponseEntity<GitlabProjectDTO> transferProject(
            @ApiParam(value = "用户id", required = true)
            @PathVariable(value = "projectId") Integer projectId,
            @ApiParam(value = "用户Id")
            @RequestParam(value = "userId") Integer userId,
            @ApiParam(value = "新的groupId")
            @RequestParam(value = "groupId") Integer groupId);

    @ApiParam(value = "下载服务源码 压缩包")
    @GetMapping(value = "/v1/projects/{projectId}/repository/archive_format")
    ResponseEntity<InputStream> downloadArchiveByFormat(
            @ApiParam(value = "用户id", required = true)
            @PathVariable(value = "projectId") Integer projectId,
            @ApiParam(value = "用户Id")
            @RequestParam(value = "user_id") Integer userId,
            @RequestParam(value = "commit_sha") String commitSha,
            @RequestParam(value = "format", required = false) String format);

    @GetMapping(value = "/v1/external_projects/query_by_code")
    ResponseEntity<GitlabProjectDTO> queryExternalProjectByCode(
            @RequestParam(value = "namespace_code") String namespaceCode,
            @RequestParam(value = "project_code") String projectCode,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

    @PutMapping(value = "/v1/external_projects/{projectId}/variables")
    ResponseEntity<List<CiVariableVO>> batchSaveExternalProjectVariable(@PathVariable("projectId") Integer projectId,
                                                                        @RequestBody List<CiVariableVO> ciVariableVOList,
                                                                        @RequestParam(value = "gitlabUrl") String gitlabUrl,
                                                                        @RequestParam(value = "authType") String authType,
                                                                        @RequestParam(value = "accessToken") String accessToken,
                                                                        @RequestParam(value = "username") String username,
                                                                        @RequestParam(value = "password") String password);

    @GetMapping(value = "/v1/groups/{group_name}/with_statistics")
    ResponseEntity<List<GroupDTO>> queryGroupWithStatisticsByName(
            @PathVariable(value = "group_name") String groupName,
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "statistics") Boolean statistics);

    @PostMapping(value = "/v1/projects/{project_id}/pipeline_schedules")
    ResponseEntity<PipelineSchedule> createPipelineSchedule(
            @PathVariable("project_id") Integer projectId,
            @RequestParam(name = "user_id",required = false) Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @RequestBody PipelineSchedule pipelineSchedule);

    @PostMapping(value = "/v1/projects/{project_id}/pipeline_schedules/{pipeline_schedule_id}/variables")
    ResponseEntity<Variable> createScheduleVariable(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Integer projectId,
            @PathVariable("pipeline_schedule_id") Integer pipelineScheduleId,
            @RequestParam(name = "user_id", required = false) Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @RequestBody Variable variable);

    @PutMapping(value = "/v1/projects/{project_id}/pipeline_schedules/{pipeline_schedule_id}/variables")
    ResponseEntity<Variable> editScheduleVariable(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Integer projectId,
            @PathVariable("pipeline_schedule_id") Integer pipelineScheduleId,
            @RequestParam(name = "user_id", required = false) Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @RequestBody Variable variable);

    @DeleteMapping(value = "/v1/projects/{project_id}/pipeline_schedules/{pipeline_schedule_id}/variables")
    ResponseEntity<Variable> deleteScheduleVariable(
            @ApiParam(value = "项目id", required = true)
            @PathVariable("project_id") Integer projectId,
            @PathVariable("pipeline_schedule_id") Integer pipelineScheduleId,
            @RequestParam(name = "user_id", required = false) Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @RequestBody Variable variable);

    @GetMapping(value = "/v1/projects/{project_id}/pipeline_schedules/{pipeline_schedule_id}")
    ResponseEntity<PipelineSchedule> queryPipelineSchedule(
            @PathVariable("project_id") Integer projectId,
            @RequestParam(name = "user_id", required = false) Integer userId,
            @PathVariable("pipeline_schedule_id") Integer pipelineScheduleId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

    @GetMapping(value = "/v1/projects/{project_id}/pipeline_schedules")
    ResponseEntity<List<PipelineSchedule>> listPipelineSchedules(
            @PathVariable("project_id") Integer projectId,
            @RequestParam(name = "user_id",required = false) Integer userId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

    @PutMapping(value = "/v1/projects/{project_id}/pipeline_schedules/{pipeline_schedule_id}")
    ResponseEntity<Void> updatePipelineSchedule(
            @PathVariable("project_id") Integer projectId,
            @RequestParam(name = "user_id", required = false) Integer userId,
            @PathVariable("pipeline_schedule_id") Integer pipelineScheduleId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @RequestBody PipelineSchedule pipelineSchedule);
    @DeleteMapping(value = "/v1/projects/{project_id}/pipeline_schedules/{pipeline_schedule_id}")
    ResponseEntity<Void> deletePipelineSchedule(
            @PathVariable("project_id") Integer projectId,
            @RequestParam(name = "user_id", required = false) Integer userId,
            @PathVariable("pipeline_schedule_id") Integer pipelineScheduleId,
            @RequestParam(value = "gitlabUrl") String gitlabUrl,
            @RequestParam(value = "authType") String authType,
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password);

    /**
     * 删除deployKeys
     *
     * @param projectId 项目Id
     * @param userId    用户Id
     * @Return List
     */
    @ApiOperation(value = "删除deployKeys")
    @DeleteMapping(value = "/v1/projects/deploy_key")
    ResponseEntity<Void> deleteDeployKeys(
            @ApiParam(value = "项目ID", required = true)
            @RequestParam Integer projectId,
            @ApiParam(value = "用户Id")
            @RequestParam(required = false) Integer userId,
            @RequestParam Integer keyId);
}
