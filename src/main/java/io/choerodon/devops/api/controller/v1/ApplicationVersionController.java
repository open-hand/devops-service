package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.devops.app.service.ApplicationVersionService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Zenger on 2018/4/3.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}")
public class ApplicationVersionController {

    private static final String VERSION_QUERY_ERROR = "error.application.version.query";
    private ApplicationVersionService applicationVersionService;

    public ApplicationVersionController(ApplicationVersionService applicationVersionService) {
        this.applicationVersionService = applicationVersionService;
    }

    /**
     * 分页查询应用版本
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return ApplicationVersionRepDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "分页查询应用版本")
    @CustomPageRequest
    @PostMapping(value = "/app_version/list_by_options")
    public ResponseEntity<Page<ApplicationVersionRepDTO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(applicationVersionService.listApplicationVersion(
                projectId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 应用下查询应用所有版本
     *
     * @param projectId 项目id
     * @param appId     应用Id
     * @return List
     */
    @ApiOperation(value = "应用下查询应用所有版本")
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @GetMapping("/apps/{appId}/version/list")
    public ResponseEntity<List<ApplicationVersionRepDTO>> queryByAppId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id")
            @PathVariable Long appId,
            @ApiParam(value = "是否发布", required = false)
            @RequestParam(value = "is_publish", required = false) Boolean isPublish) {
        return Optional.ofNullable(applicationVersionService.listByAppId(appId, isPublish))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 查询部署在某个环境的应用版本
     *
     * @param projectId 项目id
     * @param appId     应用Id
     * @param envId     环境Id
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "查询部署在某个环境应用的应用版本")
    @GetMapping("/apps/{appId}/version")
    public ResponseEntity<List<ApplicationVersionRepDTO>> queryByAppIdAndEnvId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = true)
            @PathVariable Long appId,
            @ApiParam(value = "环境 ID", required = true)
            @RequestParam Long envId) {
        return Optional.ofNullable(applicationVersionService.listByAppIdAndEnvId(projectId, appId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 分页查询某应用下的所有版本
     *
     * @param projectId   项目id
     * @param appId       应用id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return ApplicationVersionRepDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询某应用下的所有版本")
    @CustomPageRequest
    @PostMapping(value = "/apps/{appId}/version/list_by_options")
    public ResponseEntity<Page<ApplicationVersionRepDTO>> pageByApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID", required = true)
            @PathVariable Long appId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(applicationVersionService.listApplicationVersionInApp(
                projectId, appId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }
}
