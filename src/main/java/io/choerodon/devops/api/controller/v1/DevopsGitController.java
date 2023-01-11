package io.choerodon.devops.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;


/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:21
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_service/{app_service_id}/git")
public class DevopsGitController {

    @Autowired
    private DevopsGitService devopsGitService;


    /**
     * 获取应用服务的GitLab地址
     *
     * @param projectId    项目 ID
     * @param appServiceId 服务ID
     * @return url
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取应用服务的GitLab地址")
    @GetMapping("/url")
    public ResponseEntity<String> queryUrl(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(devopsGitService.queryUrl(projectId, appServiceId));
    }

    /**
     * 创建标签
     *
     * @param projectId    项目ID
     * @param appServiceId 服务ID
     * @param tag          标签名称
     * @param ref          参考名称
     * @param releaseNotes 发布日志
     * @return null
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建标签")
    @PostMapping("/tags")
    public ResponseEntity<Void> createTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "标签名称", required = true)
            @RequestParam String tag,
            @ApiParam(value = "参考名称", required = true)
            @RequestParam String ref,
            @ApiParam(value = "标签描述")
            @RequestParam(value = "message", required = false, defaultValue = "") String msg,
            @ApiParam(value = "发布日志")
            @RequestBody(required = false) String releaseNotes) {
        devopsGitService.createTag(projectId, appServiceId, tag, ref, msg, releaseNotes);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新标签
     *
     * @param projectId    项目ID
     * @param appServiceId 服务ID
     * @param tag          标签名称
     * @param releaseNotes 发布日志
     * @return null
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "更新标签")
    @PutMapping("/tags")
    public ResponseEntity<Void> updateTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "标签名称", required = true)
            @RequestParam String tag,
            @ApiParam(value = "发布日志")
            @RequestBody(required = false) String releaseNotes) {
        devopsGitService.updateTag(projectId, appServiceId, tag, releaseNotes);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 分页获取标签列表
     *
     * @param projectId    项目ID
     * @param appServiceId 服务ID
     * @param params       查询参数
     * @return PageInfo
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页获取标签列表")
    @PostMapping("/page_tags_by_options")
    public ResponseEntity<Page<TagVO>> pageTagsByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @ApiParam(value = "是否校验gitlab项目角色")
            @RequestParam(value = "checkMember", required = false, defaultValue = "true") Boolean checkMember) {
        return Results.success(devopsGitService.pageTagsByOptions(projectId, appServiceId, params, page, size, checkMember));
    }

    /**
     * 获取标签列表
     *
     * @param projectId    项目ID
     * @param appServiceId 服务ID
     * @return null
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取标签列表")
    @GetMapping("/list_tags")
    public ResponseEntity<List<TagVO>> listTags(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(devopsGitService.listTags(projectId, appServiceId));
    }

    /**
     * 检查标签
     *
     * @param projectId    项目ID
     * @param appServiceId 服务ID
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查标签")
    @GetMapping("/check_tag")
    public ResponseEntity<Boolean> checkTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "Tag 名称", required = true)
            @RequestParam(value = "tag_name") String tagName) {
        return ResponseEntity.ok(devopsGitService.checkTag(projectId, appServiceId, tagName));
    }

    /**
     * 删除标签
     *
     * @param projectId    项目Id
     * @param appServiceId 服务Id
     * @param tag          标签名
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除标签")
    @DeleteMapping("/tags")
    public ResponseEntity<Void> deleteTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "标签名称", required = true)
            @RequestParam String tag) {
        devopsGitService.deleteTag(projectId, appServiceId, tag);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 获取服务两个tag之间的issueId列表
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @param from         前一个tag
     * @param to           后一个tag
     * @return 所有的issueId
     */
    @Permission(level = ResourceLevel.ORGANIZATION, permissionWithin = true)
    @ApiOperation("获取服务两个tag之间的issueId列表")
    @GetMapping("/tags/issue_ids")
    public ResponseEntity<List<IssueIdAndBranchIdsVO>> getIssueIdsBetweenTags(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用服务id", required = true)
            @Encrypt @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "前一个tag")
            @RequestParam(value = "from", required = false) String from,
            @ApiParam(value = "后一个tag")
            @RequestParam(value = "to") String to) {
        return ResponseEntity.ok(devopsGitService.getIssueIdsBetweenTags(projectId, appServiceId, from, to));
    }

    /**
     * 创建分支
     *
     * @param projectId      项目ID
     * @param appServiceId   服务ID
     * @param devopsBranchVO 分支
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建分支")
    @PostMapping("/branch")
    public ResponseEntity<Void> createBranch(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支", required = true)
            @RequestBody DevopsBranchVO devopsBranchVO) {
        devopsGitService.createBranch(projectId, appServiceId, devopsBranchVO);
        return ResponseEntity.ok().build();
    }

    /**
     * 分页查询服务下的分支
     *
     * @param projectId        项目 ID
     * @param appServiceId     服务ID
     * @param params           查询参数
     * @param currentProjectId 当前所处项目id
     * @return PageInfo
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询服务下的分支")
    @CustomPageRequest
    @PostMapping("/page_branch_by_options")
    public ResponseEntity<Page<BranchVO>> pageBranchByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params,
            @ApiParam(value = "当前所处项目id")
            @RequestParam(value = "current_project_id", required = false) Long currentProjectId
    ) {
        return Results.success(devopsGitService.pageBranchByOptions(projectId, pageable, appServiceId, params, currentProjectId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询服务下的分支信息")
    @CustomPageRequest
    @PostMapping("/page_branch_basic_info_by_options")
    public ResponseEntity<Page<BranchVO>> pageBranchBasicInfoByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params
    ) {
        return Results.success(devopsGitService.pageBranchBasicInfoByOptions(projectId, pageable, appServiceId, params));
    }

    /**
     * 分页查询服务下的分支，并过滤掉绑定issue的分支
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @param params       查询参数
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询服务下的分支,并过滤掉绑定了指定问题分支")
    @CustomPageRequest
    @PostMapping("/page_branch_by_options_filtered_by_issue_id")
    public ResponseEntity<Page<BranchVO>> pageBranchFilteredByIssueId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params,
            @ApiParam(value = "需要过滤的issueId")
            @Encrypt @RequestParam(value = "issue_id", required = false) Long issueId
    ) {
        return Results.success(devopsGitService.pageBranchFilteredByIssueId(projectId, pageable, appServiceId, params, issueId));
    }

    /**
     * 查询单个分支
     *
     * @param projectId    项目 ID
     * @param appServiceId 服务ID
     * @param branchName   分支名
     * @return BranchDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询单个分支")
    @GetMapping("/branch")
    public ResponseEntity<DevopsBranchVO> queryBranch(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支名", required = true)
            @RequestParam(value = "branch_name") String branchName) {
        return ResponseEntity.ok(devopsGitService.queryBranch(projectId, appServiceId, branchName));
    }

    /**
     * 更新分支关联的问题
     *
     * @param projectId            项目 ID
     * @param appServiceId         服务ID
     * @param devopsBranchUpdateVO 分支更新信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "更新分支关联的问题")
    @PutMapping("/update_branch_issue")
    public ResponseEntity<Void> updateBranchIssue(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支更新信息", required = true)
            @RequestBody @Valid DevopsBranchUpdateVO devopsBranchUpdateVO,
            @ApiParam(value = "表示此次操作是否为插入")
            @RequestParam(value = "onlyInsert", required = false) boolean onlyInsert) {
        devopsGitService.updateBranchIssue(projectId, appServiceId, devopsBranchUpdateVO, onlyInsert);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除分支
     *
     * @param appServiceId 服务ID
     * @param branchName   分支名
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除分支")
    @DeleteMapping("/branch")
    public ResponseEntity<Void> deleteBranch(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支名", required = true)
            @RequestParam(value = "branch_name") String branchName) {
        devopsGitService.deleteBranch(projectId, appServiceId, branchName);
        return ResponseEntity.noContent().build();
    }

    /**
     * 删除分支和问题的关联关系
     *
     * @param projectId    项目id
     * @param appServiceId 应用id
     * @param issueId      关联问题id
     * @return
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除分支和问题的关联关系")
    @DeleteMapping("/branch/issue/remove_association")
    public ResponseEntity<Void> removeAssociation(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @Encrypt @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支id")
            @Encrypt @RequestParam("branch_id") Long branchId,
            @ApiParam(value = "关联问题id")
            @Encrypt @RequestParam("issue_id") Long issueId
    ) {
        devopsGitService.removeAssociation(projectId, appServiceId, branchId, issueId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 查看所有合并请求
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @return mergeRequest列表
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查看所有合并请求")
    @GetMapping(value = "/list_merge_request")
    @CustomPageRequest
    public ResponseEntity<MergeRequestTotalVO> listMergeRequest(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务ID")
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "合并请求状态")
            @RequestParam(value = "state", required = false) String state,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageable) {
        return Results.success(devopsGitService.listMergeRequest(projectId, appServiceId, state, pageable));
    }

    /**
     * 校验分支名唯一性
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param branchName   分支名
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验分支名唯一性")
    @GetMapping(value = "/check_branch_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务ID")
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支名")
            @RequestParam(value = "branch_name") String branchName) {
        return ResponseEntity.ok(devopsGitService.isBranchNameUnique(projectId, appServiceId, branchName));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分支同步")
    @PostMapping("/sync_branch")
    public ResponseEntity<Void> syncBranch(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务ID")
            @PathVariable(value = "app_service_id") Long appServiceId) {
        devopsGitService.syncBranch(projectId, appServiceId, true);
        return Results.success();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验分支同步")
    @GetMapping("/check_sync_branch")
    public ResponseEntity<Integer> checkSyncBranch(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务ID")
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Results.success(devopsGitService.syncBranch(projectId, appServiceId, false));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "同步开放的合并请求")
    @GetMapping("/sync_open_merge_request")
    public ResponseEntity<Integer> syncOpenMergeRequest(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务ID")
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Results.success(devopsGitService.syncOpenMergeRequest(projectId, appServiceId, true));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验开放的合并请求同步")
    @GetMapping("/check_sync_open_merge_request")
    public ResponseEntity<Integer> checkSyncOpenMergeRequest(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务ID")
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Results.success(devopsGitService.syncOpenMergeRequest(projectId, appServiceId, false));
    }
}
