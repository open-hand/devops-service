package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.BranchDTO;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.infra.dataobject.gitlab.TagDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:21
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/apps/{application_id}/git")
public class DevopsGitController {

    @Autowired
    private DevopsGitService devopsGitService;


    /**
     * 获取工程下地址
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @return url
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取工程下地址")
    @GetMapping("/url")
    public ResponseEntity<String> getUrl(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId) {
        return Optional.ofNullable(devopsGitService.getUrl(projectId, applicationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.url.get"));
    }

    /**
     * 创建标签
     *
     * @param projectId     项目ID
     * @param applicationId 应用ID
     * @param tag           标签名称
     * @param ref           参考名称
     * @return null
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建标签")
    @PostMapping("/tags")
    public ResponseEntity createTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "标签名称", required = true)
            @RequestParam String tag,
            @ApiParam(value = "参考名称", required = true)
            @RequestParam String ref) {
        devopsGitService.createTag(projectId, applicationId, tag, ref);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 获取标签列表
     *
     * @param projectId     项目ID
     * @param applicationId 应用ID
     * @return null
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取标签分页列表")
    @GetMapping("/tags")
    public ResponseEntity<Page<TagDO>> getTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,

            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return Optional.ofNullable(devopsGitService.getTags(projectId, applicationId, page, size))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.tags.get"));
    }

    /**
     * 获取标签列表
     *
     * @param projectId     项目ID
     * @param applicationId 应用ID
     * @return null
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取标签列表")
    @GetMapping("/tag_list")
    public ResponseEntity<List<TagDO>> getTagList(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId) {
        return Optional.ofNullable(devopsGitService.getTags(projectId, applicationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.tags.get"));
    }

    /**
     * 检查标签
     *
     * @param projectId     项目ID
     * @param applicationId 应用ID
     * @return null
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查标签")
    @GetMapping("/tags_check")
    public ResponseEntity<Boolean> checkTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "Tag 名称",required = true)
            @RequestParam(value = "tag_name") String tagName) {
        return Optional.ofNullable(devopsGitService.checkTag(projectId, applicationId,tagName))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.tag.check"));
    }

    /**
     * 删除标签
     *
     * @param projectId      项目Id
     * @param applicationId  应用Id
     * @param tag            标签名
     * @return null
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "删除标签")
    @DeleteMapping("/tags")
    public ResponseEntity deleteTag(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "标签名称", required = true)
            @RequestParam String tag) {
        devopsGitService.deleteTag(projectId, applicationId, tag);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 创建分支
     *
     * @param projectId       项目ID
     * @param applicationId   应用ID
     * @param devopsBranchDTO 分支
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建分支")
    @PostMapping("/branch")
    public ResponseEntity createBranch(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "分支", required = true)
            @RequestBody DevopsBranchDTO devopsBranchDTO) {
        devopsGitService.createBranch(
                projectId, applicationId, devopsBranchDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 获取工程下所有分支名
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取工程下所有分支名")
    @GetMapping("/branches")
    public ResponseEntity<List<BranchDTO>> listByAppId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId) {
        return Optional.ofNullable(devopsGitService.listBranches(projectId, applicationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.branch.get"));
    }

    /**
     * 查询单个分支
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param branchName    分支名
     * @return BranchDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询单个分支")
    @GetMapping("/branch")
    public ResponseEntity<DevopsBranchDTO> queryByAppId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "分支名", required = true)
            @RequestParam(value = "branchName") String branchName) {
        devopsGitService.queryBranch(projectId, applicationId, branchName);
        return new ResponseEntity<>(devopsGitService.queryBranch(projectId, applicationId, branchName), HttpStatus.OK);
    }

    /**
     * 更新分支关联的问题
     *
     * @param projectId       项目 ID
     * @param applicationId   应用ID
     * @param devopsBranchDTO 分支
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "更新分支关联的问题")
    @PutMapping("/branch")
    public ResponseEntity update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "分支", required = true)
            @RequestBody DevopsBranchDTO devopsBranchDTO) {
        devopsGitService.updateBranch(projectId, applicationId, devopsBranchDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 删除分支
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param branchName    分支名
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除分支")
    @DeleteMapping("/branch")
    public ResponseEntity delete(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "分支名", required = true)
            @RequestParam String branchName) {
        devopsGitService.deleteBranch(projectId, applicationId, branchName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 查看所有合并请求
     *
     * @param projectId     项目id
     * @param applicationId 应用id
     * @return mergeRequest列表
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查看所有合并请求")
    @GetMapping(value = "/merge_request/list")
    @CustomPageRequest
    public ResponseEntity<Map<String, Object>> getMergeRequestList(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID")
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "合并请求状态", required = false)
            @RequestParam(value = "state", required = false) String state,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest) {
        return Optional.ofNullable(devopsGitService.getMergeRequestList(projectId, applicationId, state, pageRequest))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.mergerequest.get"));
    }
}
