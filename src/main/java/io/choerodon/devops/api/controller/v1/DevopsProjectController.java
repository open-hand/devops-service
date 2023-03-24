package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.api.vo.sonar.SonarInfo;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.dto.GitlabProjectSimple;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author crockitwood
 * @date 2019-02-18
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}")
public class DevopsProjectController {
    @Autowired
    private DevopsProjectService devopsProjectService;

    /**
     * 查询项目Gitlab Group是否创建成功
     * 用作Demo数据初始化时查询状态
     *
     * @param projectId 项目id
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询项目Gitlab Group是否创建成功")
    @GetMapping("/check_gitlab_group")
    public ResponseEntity<Boolean> queryProjectGroupReady(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return new ResponseEntity<>(devopsProjectService.queryProjectGitlabGroupReady(projectId), HttpStatus.OK);

    }

    /**
     * 分页查询与该项目在同一组织的项目列表（包含自身）
     *
     * @param projectId 项目id
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询与该项目在同一组织的项目列表（包含自身）")
    @CustomPageRequest
    @PostMapping("/page_projects")
    public ResponseEntity<Page<ProjectReqVO>> pageProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsProjectService.pageProjects(projectId, pageable, params));
    }

    /**
     * 列出项目下的所有项目所有者和项目成员
     *
     * @param projectId 项目id
     * @param pageable  分页参数
     * @param params    查询参数
     * @return 项目所有者和项目成员
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取所有项目成员和项目所有者")
    @PostMapping(value = "/users/list_users")
    public ResponseEntity<Page<UserVO>> getAllUsers(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params
    ) {
        return ResponseEntity.ok(devopsProjectService.listAllOwnerAndMembers(projectId, pageable, params));
    }


    /**
     * 查询项目gitlab group信息
     *
     * @param projectId 项目id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, permissionWithin = true)
    @ApiOperation(value = "查询项目gitlab group信息")
    @PostMapping("/gitlab_groups")
    public ResponseEntity<List<GitlabProjectSimple>> queryGitlabGroups(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "项目Ids")
            @RequestBody List<Long> projectIds) {
        return new ResponseEntity<>(devopsProjectService.queryGitlabGroups(projectIds), HttpStatus.OK);

    }


    @Permission(permissionWithin = true)
    @ApiOperation(value = "查询项目gitlab group信息")
    @GetMapping
    public ResponseEntity<Long> queryDevopsProject(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return new ResponseEntity<>(devopsProjectService.queryDevopsProject(projectId), HttpStatus.OK);

    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下执行soanr扫描的信息")
    @GetMapping("sonar_info")
    public ResponseEntity<SonarInfo> querySonarInfo(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsProjectService.querySonarInfo(projectId));
    }

}
