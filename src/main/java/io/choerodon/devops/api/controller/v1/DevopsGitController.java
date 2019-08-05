package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.BranchVO;
import io.choerodon.devops.api.vo.DevopsBranchVO;
import io.choerodon.devops.api.vo.MergeRequestTotalVO;
import io.choerodon.devops.api.vo.TagVO;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;


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
     * 获取工程下地址
     *
     * @param projectId     项目 ID
     * @param appServiceId 服务ID
     * @return url
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取工程下地址")
    @GetMapping("/url")
    public ResponseEntity<String> queryUrl(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(devopsGitService.queryUrl(projectId, appServiceId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.url.get"));
    }

    /**
     * 创建标签
     *
     * @param projectId     项目ID
     * @param appServiceId 服务ID
     * @param tag           标签名称
     * @param ref           参考名称
     * @param releaseNotes  发布日志
     * @return null
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建标签")
    @PostMapping("/tags")
    public ResponseEntity createTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
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
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 更新标签
     *
     * @param projectId     项目ID
     * @param appServiceId 服务ID
     * @param tag           标签名称
     * @param releaseNotes  发布日志
     * @return null
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "更新标签")
    @PutMapping("/tags")
    public ResponseEntity updateTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
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
     * @param projectId     项目ID
     * @param appServiceId 服务ID
     * @param params        查询参数
     * @return PageInfo
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页获取标签列表")
    @PostMapping("/page_tags_by_options")
    public ResponseEntity<PageInfo<TagVO>> pageTagsByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return Optional.ofNullable(devopsGitService.pageTagsByOptions(projectId, appServiceId, params,
                page, size))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.tags.get"));
    }

    /**
     * 获取标签列表
     *
     * @param projectId     项目ID
     * @param appServiceId 服务ID
     * @return null
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取标签列表")
    @GetMapping("/list_tags")
    public ResponseEntity<List<TagVO>> listTags(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(devopsGitService.listTags(projectId, appServiceId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.tags.get"));
    }

    /**
     * 检查标签
     *
     * @param projectId     项目ID
     * @param appServiceId 服务ID
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查标签")
    @GetMapping("/check_tag")
    public ResponseEntity<Boolean> checkTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
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
     * @param projectId     项目Id
     * @param appServiceId 服务Id
     * @param tag           标签名
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除标签")
    @DeleteMapping("/tags")
    public ResponseEntity deleteTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
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
     * @param appServiceId  服务ID
     * @param devopsBranchVO 分支
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建分支")
    @PostMapping("/branch")
    public ResponseEntity createBranch(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支", required = true)
            @RequestBody DevopsBranchVO devopsBranchVO) {
        devopsGitService.createBranch(projectId, appServiceId, devopsBranchVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 分页查询服务下的分支
     *
     * @param projectId     项目 ID
     * @param appServiceId 服务ID
     * @param params        查询参数
     * @return PageInfo
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询服务下的分支")
    @CustomPageRequest
    @PostMapping("/page_branch_by_options")
    public ResponseEntity<PageInfo<BranchVO>> pageBranchByOptions(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsGitService.pageBranchByOptions(projectId, pageRequest, appServiceId, params))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.branch.get"));
    }

    /**
     * 查询单个分支
     *
     * @param projectId     项目 ID
     * @param appServiceId 服务ID
     * @param branchName    分支名
     * @return BranchDTO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询单个分支")
    @GetMapping("/branch")
    public ResponseEntity<DevopsBranchVO> queryBranch(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
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
     * @param projectId      项目 ID
     * @param appServiceId  服务ID
     * @param devopsBranchVO 分支
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "更新分支关联的问题")
    @PutMapping("/update_branch_issue")
    public ResponseEntity updateBranchIssue(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支", required = true)
            @RequestBody DevopsBranchVO devopsBranchVO) {
        devopsGitService.updateBranchIssue(projectId, appServiceId, devopsBranchVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除分支
     *
     * @param appServiceId 服务ID
     * @param branchName    分支名
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除分支")
    @DeleteMapping("/branch")
    public ResponseEntity deleteBranch(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支名", required = true)
            @RequestParam(value = "branch_name") String branchName) {
        devopsGitService.deleteBranch(appServiceId, branchName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 查看所有合并请求
     *
     * @param projectId     项目id
     * @param appServiceId 服务id
     * @return mergeRequest列表
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查看所有合并请求")
    @GetMapping(value = "/list_merge_request")
    @CustomPageRequest
    public ResponseEntity<MergeRequestTotalVO> listMergeRequest(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务ID")
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "合并请求状态")
            @RequestParam(value = "state", required = false) String state,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageRequest) {
        return Optional.ofNullable(devopsGitService.listMergeRequest(projectId, appServiceId, state, pageRequest))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.mergerequest.get"));
    }

    /**
     * 校验分支名唯一性
     *
     * @param projectId     项目id
     * @param appServiceId 服务id
     * @param branchName    分支名
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "校验分支名唯一性")
    @GetMapping(value = "/check_branch_name")
    public void checkName(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务ID")
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支名")
            @RequestParam(value = "branch_name") String branchName) {
        devopsGitService.checkBranchName(projectId, appServiceId, branchName);
    }
}
