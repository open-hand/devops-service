package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.BranchDTO;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.app.service.DevopsGitService;
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
    public ResponseEntity start(
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
     * @param devopsBranchDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "更新分支关联的问题")
    @PutMapping("/branch")
    public ResponseEntity queryByAppId(
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
}
