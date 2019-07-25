package io.choerodon.devops.api.controller.v1;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.Sort;
import io.choerodon.devops.api.vo.AppVersionAndValueVO;
import io.choerodon.devops.api.vo.ApplicationVersionRespVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.ApplicationVersionAndCommitVO;
import io.choerodon.devops.api.vo.DeployVersionVO;
import io.choerodon.devops.app.service.ApplicationVersionService;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * Created by Zenger on 2018/4/3.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_versions")
public class ApplicationVersionController {

    private static final String VERSION_QUERY_ERROR = "error.application.version.query";

    @Autowired
    private ApplicationVersionService applicationVersionService;


    /**
     * 分页查询应用版本
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param appId       应用Id，选填的用于进行筛选记录的字段
     * @param searchParam 查询参数
     * @return ApplicationVersionRespVO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询应用版本")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<PageInfo<ApplicationVersionRespVO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "应用Id，选填的用于进行筛选记录的字段")
            @RequestParam(value = "app_id", required = false) Long appId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(applicationVersionService.pageApplicationVersionInApp(
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
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @CustomPageRequest
    @GetMapping("/page_by_app/{app_id}")
    public ResponseEntity<PageInfo<ApplicationVersionRespVO>> queryByAppId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id")
            @PathVariable(value = "app_id") Long appId,
            @ApiParam(value = "应用版本Id")
            @RequestParam(value = "app_version_id", required = false) Long appVersionId,
            @ApiParam(value = "是否发布")
            @RequestParam(value = "is_publish", required = false) Boolean isPublish,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestParam(value = "version", required = false) String searchParam) {
        return Optional.ofNullable(applicationVersionService.pageByAppIdAndParam(appId, isPublish, appVersionId, pageRequest, searchParam))
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
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @GetMapping("/list_deployed_by_app/{app_id}")
    public ResponseEntity<List<ApplicationVersionRespVO>> queryDeployedByAppId(
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
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询部署在某个环境应用的应用版本")
    @GetMapping("/app/{app_id}/env/{envId}/query")
    public ResponseEntity<List<ApplicationVersionRespVO>> queryByAppIdAndEnvId(
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
     * @return ApplicationVersionRespVO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "实例下查询可升级版本")
    @GetMapping(value = "/version/{app_version_id}/upgrade_version")
    public ResponseEntity<List<ApplicationVersionRespVO>> listUpgradeableAppVersion(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用版本ID", required = true)
            @PathVariable(value = "app_version_id") Long appVersionId) {
        return Optional.ofNullable(applicationVersionService.listUpgradeableAppVersion(projectId, appVersionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 项目下查询应用最新的版本和各环境下部署的版本
     *
     * @param projectId 项目ID
     * @param appId     应用ID
     * @return DeployVersionVO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询应用最新的版本和各环境下部署的版本")
    @GetMapping(value = "/app/{app_id}/deployVersions")
    public ResponseEntity<DeployVersionVO> queryDeployedVersions(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID", required = true)
            @PathVariable(value = "app_id") Long appId) {
        return Optional.ofNullable(applicationVersionService.queryDeployedVersions(appId))
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
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据版本id获取版本values")
    @GetMapping(value = "/{app_versionId}/queryValue")
    public ResponseEntity<String> queryVersionValue(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用版本ID", required = true)
            @PathVariable(value = "app_versionId") Long appVersionId) {
        return Optional.ofNullable(applicationVersionService.queryVersionValue(appVersionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.value.query"));
    }


    /**
     * 根据版本id查询版本信息
     *
     * @param projectId     项目ID
     * @param appVersionIds 应用版本ID
     * @return ApplicationVersionRespVO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据版本id查询版本信息")
    @PostMapping(value = "/list_by_appVersionIds")
    public ResponseEntity<List<ApplicationVersionRespVO>> queryAppVersionsByIds(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID", required = true)
            @RequestParam Long[] appVersionIds) {
        return Optional.ofNullable(applicationVersionService.listByAppVersionIds(Arrays.asList(appVersionIds)))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }


    /**
     * 根据分支名查询版本
     *
     * @param projectId 项目ID
     * @param branch    分支
     * @param appId     应用Id
     * @return ApplicationVersionRespVO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据分支名查询版本")
    @GetMapping(value = "/list_by_branch")
    public ResponseEntity<List<ApplicationVersionAndCommitVO>> listAppVersionsByBranch(
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
     * 根据pipelineID 查询版本, 判断是否存在
     *
     * @param projectId  项目ID
     * @param pipelineId 持续集成Id
     * @param branch     分支
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据pipelineID 查询版本, 判断是否存在")
    @GetMapping(value = "/query_by_pipeline")
    public ResponseEntity<Boolean> queryByPipeline(
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "持续集成Id", required = true)
            @RequestParam(value = "pipeline_id") Long pipelineId,
            @ApiParam(value = "应用Id", required = true)
            @RequestParam(value = "app_id") Long appId,
            @ApiParam(value = "分支", required = true)
            @RequestParam String branch) {
        return Optional.ofNullable(applicationVersionService.queryByPipelineId(pipelineId, branch, appId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 根据应用ID查询最新生成版本
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @return 最新版本
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据应用ID查询最新生成版本")
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
     * @param projectId 项目ID
     * @param appId     应用Id
     * @param version   版本
     * @return ApplicationVersionRespVO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据应用和版本号查询应用版本")
    @GetMapping(value = "/query_by_version")
    public ResponseEntity<ApplicationVersionRespVO> queryByVersion(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = true)
            @RequestParam(value = "app_id") Long appId,
            @ApiParam(value = "版本号", required = true)
            @RequestParam String version) {
        return Optional.ofNullable(applicationVersionService.queryByAppAndVersion(appId, version))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询远程应用版本")
    @CustomPageRequest
    @PostMapping(value = "/page_remote/versions")
    public ResponseEntity<PageInfo<ApplicationVersionRespVO>> pageVersionByAppId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = true)
            @RequestParam(value = "app_id") Long appId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestParam(value = "version",required = false) String version) {
        return Optional.ofNullable(
                applicationVersionService.pageVersionByAppId(appId, pageRequest, version))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.remote.application.versions.get"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询远程应用版本详情")
    @CustomPageRequest
    @PostMapping(value = "/remote/config")
    public ResponseEntity<AppVersionAndValueVO> queryConfigByVerionId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = true)
            @RequestParam(value = "app_id") Long appId,
            @ApiParam(value = "版本Id", required = true)
            @RequestParam(name = "version_id") Long versionId) {
        return Optional.ofNullable(
                applicationVersionService.queryConfigByVerionId(appId, versionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.remote.version.config.get"));
    }

}
