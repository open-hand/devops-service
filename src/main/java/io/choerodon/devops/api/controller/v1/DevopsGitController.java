package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;
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
import io.choerodon.core.exception.CommonException;
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
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(devopsGitService.queryUrl(projectId, appServiceId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.url.get"));
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
            @Encrypt
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
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "标签名称", required = true)
            @RequestParam String tag,
            @ApiParam(value = "发布日志")
            @RequestBody String releaseNotes) {
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
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return Results.success(devopsGitService.pageTagsByOptions(projectId, appServiceId, params, page, size));
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
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(devopsGitService.listTags(projectId, appServiceId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.tags.get"));
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
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "Tag 名称", required = true)
            @RequestParam(value = "tag_name") String tagName) {
        return Optional.ofNullable(devopsGitService.checkTag(projectId, appServiceId, tagName))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.tag.check"));
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
            @Encrypt
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
            @Encrypt
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
     * @param projectId    项目 ID
     * @param appServiceId 服务ID
     * @param params       查询参数
     * @return PageInfo
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询服务下的分支")
    @CustomPageRequest
    @PostMapping("/page_branch_by_options")
    public ResponseEntity<Page<BranchVO>> pageBranchByOptions(
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Results.success(devopsGitService.pageBranchByOptions(projectId, pageable, appServiceId, params));
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
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支名", required = true)
            @RequestParam(value = "branch_name") String branchName) {
        return Optional.ofNullable(devopsGitService.queryBranch(projectId, appServiceId, branchName))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.branch.get"));
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
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支更新信息", required = true)
            @RequestBody @Valid DevopsBranchUpdateVO devopsBranchUpdateVO) {
        devopsGitService.updateBranchIssue(projectId, appServiceId, devopsBranchUpdateVO);
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
            @Encrypt
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
            @Encrypt
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
            @Encrypt
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务ID")
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支名")
            @RequestParam(value = "branch_name") String branchName) {
        return ResponseEntity.ok(devopsGitService.isBranchNameUnique(projectId, appServiceId, branchName));
    }
}
