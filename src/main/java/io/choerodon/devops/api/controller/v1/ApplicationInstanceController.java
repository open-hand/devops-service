package io.choerodon.devops.api.controller.v1;

import java.util.List;
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
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.domain.application.valueobject.ReplaceResult;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Zenger on 2018/4/3.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_instances")
public class ApplicationInstanceController {

    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;

    /**
     * 分页查询应用部署
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param envId       环境id
     * @param versionId   版本id
     * @param appId       应用id
     * @param params      搜索参数
     * @return page of applicationInstanceDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "分页查询应用部署")
    @CustomPageRequest
    @PostMapping(value = "/list_by_options")
    public ResponseEntity<Page<ApplicationInstanceDTO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiIgnore
            @ApiParam(value = "分页参数")
                    PageRequest pageRequest,
            @ApiParam(value = "环境ID")
            @RequestParam(required = false) Long envId,
            @ApiParam(value = "版本ID")
            @RequestParam(required = false) Long versionId,
            @ApiParam(value = "应用ID")
            @RequestParam(required = false) Long appId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(applicationInstanceService.listApplicationInstance(
                projectId, pageRequest, envId, versionId, appId, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.version.query"));
    }

    /**
     * 查询应用部署
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @return page of ApplicationInstancesDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "查询多应用部署")
    @GetMapping(value = "/all")
    public ResponseEntity<List<ApplicationInstancesDTO>> listByAppId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID")
            @RequestParam(required = false) Long appId) {
        return Optional.ofNullable(applicationInstanceService.listApplicationInstances(projectId, appId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.version.query"));
    }

    /**
     * 获取部署 Value
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @return string
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER, InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "获取部署 Value")
    @GetMapping(value = "/{appInstanceId}/value")
    public ResponseEntity<ReplaceResult> queryValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return Optional.ofNullable(applicationInstanceService.queryValue(appInstanceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.instance.value.get"));
    }

    /**
     * 查询value列表
     *
     * @param projectId    项目id
     * @param appId        应用id
     * @param envId        环境id
     * @param appVersionId 版本id
     * @return ReplaceResult
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "查询value列表")
    @GetMapping("/value")
    public ResponseEntity<ReplaceResult> queryValues(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID", required = true)
            @RequestParam Long appId,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "版本ID", required = true)
            @RequestParam Long appVersionId) {
        return Optional.ofNullable(applicationInstanceService.queryValues(appId, envId, appVersionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.values.query"));
    }

    /**
     * 预览values
     *
     * @param projectId     项目id
     * @param replaceResult 部署value
     * @return ReplaceResult
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "查询预览value")
    @PostMapping("/previewValue")
    public ResponseEntity<ReplaceResult> previewValues(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "replaceResult", required = true)
            @RequestBody ReplaceResult replaceResult,
            @ApiParam(value = "版本ID", required = true)
            @RequestParam Long appVersionId) {
        return Optional.ofNullable(applicationInstanceService.previewValues(replaceResult, appVersionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.values.query"));
    }

    /**
     * 校验values
     *
     * @param replaceResult values对象
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "校验values")
    @PostMapping("/value_format")
    public ResponseEntity<List<ErrorLineDTO>> formatValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "value", required = true)
            @RequestBody ReplaceResult replaceResult) {
        return new ResponseEntity<>(applicationInstanceService.formatValue(replaceResult), HttpStatus.OK);
    }

    /**
     * 部署应用
     *
     * @param projectId            项目id
     * @param applicationDeployDTO 部署信息
     * @return ApplicationInstanceDTO
     */
    @ApiOperation(value = "部署应用")
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @PostMapping
    public ResponseEntity<ApplicationInstanceDTO> deploy(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署信息", required = true)
            @RequestBody ApplicationDeployDTO applicationDeployDTO) {
        return Optional.ofNullable(applicationInstanceService.create(applicationDeployDTO, false))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.deploy"));
    }

    /**
     * 获取版本特性
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @return list of versionFeaturesDTO
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "获取版本特性")
    @GetMapping("/{appInstanceId}/version_features")
    public ResponseEntity<List<VersionFeaturesDTO>> queryVersionFeatures(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return Optional.ofNullable(applicationInstanceService.queryVersionFeatures(appInstanceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.version.values.query"));
    }

    /**
     * 查询运行中的实例
     *
     * @param projectId    项目id
     * @param appId        应用id
     * @param appVersionId 应用版本id
     * @param envId        环境id
     * @return list of AppInstanceCodeDTO
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "查询运行中的实例")
    @GetMapping("/options")
    public ResponseEntity<List<AppInstanceCodeDTO>> listByAppVersionId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID")
            @RequestParam(required = false) Long envId,
            @ApiParam(value = "应用Id")
            @RequestParam(required = false) Long appId,
            @ApiParam(value = "应用版本 ID")
            @RequestParam(required = false) Long appVersionId) {
        return Optional.ofNullable(applicationInstanceService.listByOptions(projectId, appId, appVersionId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.appInstance.query"));
    }


    /**
     * 获取部署实例资源对象
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @return DevopsEnvResourceDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER, InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "获取部署实例资源对象")
    @GetMapping("/{appInstanceId}/resources")
    public ResponseEntity<DevopsEnvResourceDTO> listResources(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable Long appInstanceId) {
        return Optional.ofNullable(devopsEnvResourceService.listResources(appInstanceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.resource.query"));
    }

    /**
     * 获取部署实例hook阶段
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @return list
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER, InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "获取部署实例hook阶段")
    @GetMapping("/{appInstanceId}/stages")
    public ResponseEntity<List<InstanceStageDTO>> listStages(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable Long appInstanceId) {
        return Optional.ofNullable(devopsEnvResourceService.listStages(appInstanceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.stage.query"));
    }

    /**
     * 实例停止
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "实例停止")
    @PutMapping(value = "/{instanceId}/stop")
    public ResponseEntity stop(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable Long instanceId) {
        applicationInstanceService.instanceStop(instanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 实例重启
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "实例重启")
    @PutMapping(value = "/{instanceId}/start")
    public ResponseEntity start(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable Long instanceId) {
        applicationInstanceService.instanceStart(instanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 实例删除
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "实例删除")
    @DeleteMapping(value = "/{instanceId}/delete")
    public ResponseEntity delete(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable Long instanceId) {
        applicationInstanceService.instanceDelete(instanceId, false);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    /**
     * 环境总览实例查询
     *
     * @param projectId 项目id
     * @param envId     环境Id
     * @param params    搜索参数
     * @return DevopsEnvPreviewDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "环境总览实例查询")
    @PostMapping(value = "/{envId}/listByEnv")
    public ResponseEntity<DevopsEnvPreviewDTO> listByEnv(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "envId", required = true)
            @PathVariable(value = "envId") Long envId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(applicationInstanceService.listByEnv(projectId, envId, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.appInstance.query"));
    }


//    /**
//     * 查询实例下容器,网络和域名
//     *
//     * @param projectId  项目id
//     * @param instanceId 实例id
//     * @return DevopsEnvPreviewDTO
//     */
//    @Permission(level = ResourceLevel.PROJECT,
//            roles = {InitRoleCode.PROJECT_OWNER,
//                    InitRoleCode.PROJECT_MEMBER,
//                    InitRoleCode.DEPLOY_ADMINISTRATOR})
//    @ApiOperation(value = "查询实例下容器,网络和域名")
//    @GetMapping(value = "/{instanceId}/InstanceResource")
//    public ResponseEntity<DevopsEnvPreviewInstanceDTO> getDevopsEnvPreviewInstance(
//            @ApiParam(value = "项目 ID", required = true)
//            @PathVariable(value = "project_id") Long projectId,
//            @ApiParam(value = "instanceId", required = true)
//            @PathVariable(value = "instanceId") Long instanceId) {
//        return Optional.ofNullable(applicationInstanceService.getDevopsEnvPreviewInstance(instanceId))
//                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
//                .orElseThrow(() -> new CommonException("error.appInstance.query"));
//    }


    /**
     * 部署文件日志
     *
     * @param projectId 项目id
     * @param envId     实例id
     * @return DevopsEnvPreviewDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "部署文件日志")
    @GetMapping(value = "/{envId}/envFiles")
    public ResponseEntity<List<DevopsEnvFileDTO>> getDevopsEnvPreviewInstance(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "envId", required = true)
            @PathVariable(value = "envId") Long envId) {
        return Optional.ofNullable(applicationInstanceService.getEnvFile(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.file.query"));
    }


}
