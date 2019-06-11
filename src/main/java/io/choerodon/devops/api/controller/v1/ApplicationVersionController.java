package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.ApplicationVersionAndCommitDTO;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.devops.api.dto.DeployVersionDTO;
import io.choerodon.devops.app.service.ApplicationVersionService;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by Zenger on 2018/4/3.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_versions")
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
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询应用版本")
    @CustomPageRequest
    @PostMapping(value = "/list_by_options")
    public ResponseEntity<PageInfo<ApplicationVersionRepDTO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "应用Id")
            @RequestParam(required = false) Long appId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(applicationVersionService.listApplicationVersionInApp(
                projectId, appId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 应用下查询应用所有版本
     *
     * @param projectId    项目id
     * @param appId        应用Id
     * @param appVersionId 应用版本Id
     * @param isPublish    版本是否发布
     * @param pageRequest  分页参数
     * @param searchParam  查询参数
     * @return List
     */
    @ApiOperation(value = "应用下查询应用所有版本")
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @CustomPageRequest
    @GetMapping("/list_by_app/{app_id}")
    public ResponseEntity<PageInfo<ApplicationVersionRepDTO>> queryByAppId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id")
            @PathVariable(value = "app_id") Long appId,
            @ApiParam(value = "应用版本Id", required = false)
            @RequestParam(value = "app_version_id", required = false) Long appVersionId,
            @ApiParam(value = "是否发布", required = false)
            @RequestParam(value = "is_publish", required = false) Boolean isPublish,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestParam(value = "version", required = false) String searchParam) {
        return Optional.ofNullable(applicationVersionService.listByAppIdAndParamWithPage(appId, isPublish, appVersionId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 项目下查询应用所有已部署版本
     *
     * @param projectId 项目id
     * @param appId     应用Id
     * @return List
     */
    @ApiOperation(value = "项目下查询应用所有已部署版本")
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @GetMapping("/list_deployed_by_app/{app_id}")
    public ResponseEntity<List<ApplicationVersionRepDTO>> queryDeployedByAppId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id")
            @PathVariable(value = "app_id") Long appId) {
        return Optional.ofNullable(applicationVersionService.listDeployedByAppId(projectId, appId))
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
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询部署在某个环境应用的应用版本")
    @GetMapping("/app/{app_id}/env/{envId}/query")
    public ResponseEntity<List<ApplicationVersionRepDTO>> queryByAppIdAndEnvId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = true)
            @PathVariable(value = "app_id") Long appId,
            @ApiParam(value = "环境 ID", required = true)
            @PathVariable Long envId) {
        return Optional.ofNullable(applicationVersionService.listByAppIdAndEnvId(projectId, appId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 根据应用版本ID查询，可升级的应用版本
     *
     * @param projectId    项目ID
     * @param appVersionId 应用版本ID
     * @return ApplicationVersionRepDTO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "实例下查询可升级版本")
    @GetMapping(value = "/version/{app_version_id}/upgrade_version")
    public ResponseEntity<List<ApplicationVersionRepDTO>> getUpgradeAppVersion(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用版本ID", required = true)
            @PathVariable(value = "app_version_id") Long appVersionId) {
        return Optional.ofNullable(applicationVersionService.getUpgradeAppVersion(projectId, appVersionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 项目下查询应用最新的版本和各环境下部署的版本
     *
     * @param projectId 项目ID
     * @param appId     应用ID
     * @return DeployVersionDTO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询应用最新的版本和各环境下部署的版本")
    @GetMapping(value = "/app/{app_id}/deployVersions")
    public ResponseEntity<DeployVersionDTO> getDeployVersions(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID", required = true)
            @PathVariable(value = "app_id") Long appId) {
        return Optional.ofNullable(applicationVersionService.listDeployVersions(appId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }


    /**
     * 根据版本id获取版本values
     *
     * @param projectId    项目ID
     * @param appVersionId 应用版本ID
     * @return String
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据版本id获取版本values")
    @GetMapping(value = "/{app_verisonId}/queryValue")
    public ResponseEntity<String> getVersionValue(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用版本ID", required = true)
            @PathVariable(value = "app_verisonId") Long appVersionId) {
        return Optional.ofNullable(applicationVersionService.queryVersionValue(appVersionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.value.query"));
    }


    /**
     * 根据版本id查询版本信息
     *
     * @param projectId     项目ID
     * @param appVersionIds 应用版本ID
     * @return ApplicationVersionRepDTO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据版本id查询版本信息")
    @PostMapping(value = "/list_by_appVersionIds")
    public ResponseEntity<List<ApplicationVersionRepDTO>> getAppversion(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID", required = true)
            @RequestBody List<Long> appVersionIds) {
        return Optional.ofNullable(applicationVersionService.listByAppVersionIds(appVersionIds))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }


    /**
     * 根据分支名查询版本
     *
     * @param projectId 项目ID
     * @param branch    分支
     * @param appId     应用Id
     * @return ApplicationVersionRepDTO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据分支名查询版本")
    @GetMapping(value = "/list_by_branch")
    public ResponseEntity<List<ApplicationVersionAndCommitDTO>> getAppversionByBranch(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID", required = true)
            @RequestParam Long appId,
            @ApiParam(value = "分支", required = true)
            @RequestParam String branch) {
        return Optional.ofNullable(applicationVersionService.listByAppIdAndBranch(appId, branch))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }


    /**
     * 根据pipelineID 查询版本
     *
     * @param projectId  项目ID
     * @param pipelineId 持续集成Id
     * @param branch     分支
     * @return Boolean
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据pipelineID 查询版本")
    @GetMapping(value = "/query_by_pipeline")
    public ResponseEntity<Boolean> queryByPipeline(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "持续集成Id", required = true)
            @RequestParam Long pipelineId,
            @ApiParam(value = "分支", required = true)
            @RequestParam String branch) {
        return Optional.ofNullable(applicationVersionService.queryByPipelineId(pipelineId, branch))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 根据应用ID查询最新生成版本
     *
     * @param projectId
     * @param appId
     * @return
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询所有自动部署")
    @GetMapping("/value")
    public ResponseEntity<String> queryByProjectId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = true)
            @RequestParam(value = "app_id") Long appId) {
        return Optional.ofNullable(
                applicationVersionService.queryValueById(projectId, appId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.version.value.query"));
    }

    /**
     * 根据应用和版本号查询应用版本
     *
     * @param projectId  项目ID
     * @param appId  应用Id
     * @param version  版本
     * @return ApplicationVersionRepDTO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据应用和版本号查询应用版本")
    @GetMapping(value = "/query_by_version")
    public ResponseEntity<ApplicationVersionRepDTO> queryByVersion(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = true)
            @RequestParam Long appId,
            @ApiParam(value = "版本号", required = true)
            @RequestParam String version) {
        return Optional.ofNullable(applicationVersionService.queryByAppAndVersion(appId, version))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

}
