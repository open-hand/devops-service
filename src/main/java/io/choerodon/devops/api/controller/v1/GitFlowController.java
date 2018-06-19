package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.core.iam.InitRoleCode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.GitFlowService;
import io.choerodon.devops.domain.application.entity.gitlab.GitFlowE;
import io.choerodon.devops.infra.dataobject.gitlab.TagsDO;
import io.choerodon.swagger.annotation.Permission;

/**
 * Creator: Runge
 * Date: 2018/4/12
 * Time: 16:33
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/apps/{applicationId}/git_flow")
public class GitFlowController {

    private GitFlowService gitFlowService;

    public GitFlowController(GitFlowService gitFlowService) {
        this.gitFlowService = gitFlowService;
    }

    /**
     * 获取工程下所有分支名
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @return GitFlow 列表
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取工程下所有分支名")
    @GetMapping("/branches")
    public ResponseEntity<List<GitFlowE>> listByAppId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long applicationId) {
        return Optional.ofNullable(gitFlowService.getBranches(projectId, applicationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.branch.get"));
    }

    /**
     * 刷新分支合并请求
     *
     * @param projectId     项目 ID
     * @param applicationId 应用 ID
     * @param branch        分支名称
     * @return 合并请求状态
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("刷新分支合并请求")
    @PostMapping("/update_merge_request_status")
    public ResponseEntity<String> finish(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long applicationId,
            @ApiParam(value = "分支名称", required = true)
            @RequestParam(value = "branch") String branch) {
        return Optional.ofNullable(
                gitFlowService.updateMRStatus(applicationId, branch))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.branch.update"));
    }

    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "开始 GitFlow")
    @PostMapping("/start")
    public ResponseEntity start(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long applicationId,
            @ApiParam(value = "分支名称", required = true)
            @RequestParam String name) {
        gitFlowService.startGitFlow(applicationId, name);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("结束Feature分支")
    @PostMapping("/finish_feature")
    public ResponseEntity finishFeatureEvent(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long applicationId,
            @ApiParam(value = "分支名称", required = true)
            @RequestParam(value = "branch") String branch) {
        gitFlowService.finishGitFlowFeature(applicationId, branch);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "结束 GitFlow")
    @PostMapping("/finish")
    public ResponseEntity finishEvent(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long applicationId,
            @ApiParam(value = "分支名称", required = true)
            @RequestParam(value = "branch") String branch) {
        gitFlowService.finishGitFlow(applicationId, branch);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 获取应用下所有 tag
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param page          页
     * @param size          页大小
     * @return Tag List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取应用下所有 tag")
    @GetMapping("/tags")
    public ResponseEntity<TagsDO> queryTags(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long applicationId,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return Optional.ofNullable(gitFlowService.getTags(projectId, applicationId, page, size))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.tag.get"));
    }

    /**
     * 获取新发布版本号
     *
     * @param projectId     项目ID
     * @param applicationId 应用ID
     * @param branch        分支名称
     * @return 下一发布版本号
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取新发布版本号")
    @GetMapping("/tags/release")
    public ResponseEntity<String> queryReleaseNumber(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long applicationId,
            @ApiParam(value = "分支名称", required = false)
            @RequestParam(value = "branch", required = false) String branch) {
        return Optional.ofNullable(gitFlowService.getReleaseNumber(applicationId, branch))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.release.tag.get"));
    }

    /**
     * 获取热修复版本号
     *
     * @param projectId     项目ID
     * @param applicationId 应用ID
     * @param branch        分支名称
     * @return 下一热修复版本号
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取热修复版本号")
    @GetMapping("/tags/hotfix")
    public ResponseEntity<String> queryHotfixNumber(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long applicationId,
            @ApiParam(value = "分支名称", required = false)
            @RequestParam(value = "branch", required = false) String branch) {
        return Optional.ofNullable(gitFlowService.getHotfixNumber(applicationId, branch))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.hotfix.tag.get"));
    }
}
