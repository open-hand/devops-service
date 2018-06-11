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
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DeployDetailService;
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

    private ApplicationInstanceService applicationInstanceService;
    private DeployDetailService deployDetailService;
    private DevopsEnvResourceService devopsEnvResourceService;

    /**
     * 构造函数
     */
    public ApplicationInstanceController(ApplicationInstanceService applicationInstanceService,
                                         DeployDetailService deployDetailService,
                                         DevopsEnvResourceService devopsEnvResourceService) {
        this.applicationInstanceService = applicationInstanceService;
        this.deployDetailService = deployDetailService;
        this.devopsEnvResourceService = devopsEnvResourceService;
    }

    /**
     * 分页查询应用部署
     *
     * @param projectId   项目id
     * @param pageRequest 项目id
     * @param envId       环境id
     * @param versionId   版本id
     * @param appId       应用id
     * @param params      分页参数
     * @return page of applicationInstanceDTO
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "分页查询应用部署")
    @CustomPageRequest
    @PostMapping(value = "/list_by_options")
    public ResponseEntity<Page<ApplicationInstanceDTO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiIgnore
            @ApiParam(value = "分页参数") PageRequest pageRequest,
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
    @Permission(level = ResourceLevel.PROJECT)
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
     * 获取容器列表
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @return list of DevOpsEnvPodDTO
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "获取容器列表")
    @GetMapping(value = "/{appInstanceId}/pods")
    public ResponseEntity<List<DevopsEnvPodDTO>> listByAppInstanceId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return Optional.ofNullable(deployDetailService.getPods(appInstanceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.instance.pods.get"));
    }


    /**
     * 获取部署 Value
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @return string
     */
    @Permission(level = ResourceLevel.PROJECT)
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
    @Permission(level = ResourceLevel.PROJECT)
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
     *校验values
     *
     * @param  value values值
     * @return String
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "校验values")
    @GetMapping("/value_format")
    public ResponseEntity<String> queryValues(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用ID", required = true)
            @RequestParam String value) {
        return Optional.ofNullable(applicationInstanceService.formatValue(value))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.values.query"));
    }

    /**
     * 部署应用
     *
     * @param projectId            项目id
     * @param applicationDeployDTO 部署信息
     * @return Boolean
     */
    @ApiOperation(value = "部署应用")
    @Permission(level = ResourceLevel.PROJECT)
    @PostMapping
    public ResponseEntity<Boolean> deploy(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署信息", required = true)
            @RequestBody ApplicationDeployDTO applicationDeployDTO) {
        return Optional.ofNullable(applicationInstanceService.create(applicationDeployDTO))
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "获取版本特性")
    @GetMapping("/{appInstanceId}/version_features")
    public ResponseEntity<List<VersionFeaturesDTO>> queryVersionFeatures(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return Optional.ofNullable(applicationInstanceService.queryVersionFeatures(appInstanceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.values.query"));
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
    @Permission(level = ResourceLevel.PROJECT)
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
    @Permission(level = ResourceLevel.PROJECT)
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
    @Permission(level = ResourceLevel.PROJECT)
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
     * 获取部署实例hook阶段
     *
     * @param projectId    项目id
     * @param instanceId   实例id
     * @param repoURL      仓库地址
     * @param chartName    Chart 名称
     * @param chartVersion Chart 版本
     * @param values       配置信息
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "实例更新")
    @PutMapping(value = "/{instanceId}/upgrade")
    public ResponseEntity upgrade(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable Long instanceId,
            @ApiParam(value = "仓库地址", required = true)
            @RequestParam String repoURL,
            @ApiParam(value = "Chart 名称", required = true)
            @RequestParam String chartName,
            @ApiParam(value = "Chart 版本", required = true)
            @RequestParam String chartVersion,
            @ApiParam(value = "配置信息", required = true)
            @RequestParam String values) {
        applicationInstanceService.instanceUpgrade(instanceId, repoURL, chartName, chartVersion, values);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 实例停止
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.PROJECT)
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
    @Permission(level = ResourceLevel.PROJECT)
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
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "实例删除")
    @DeleteMapping(value = "/{instanceId}/delete")
    public ResponseEntity delete(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable Long instanceId) {
        applicationInstanceService.instanceDelete(instanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
