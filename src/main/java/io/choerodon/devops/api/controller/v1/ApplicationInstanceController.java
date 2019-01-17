package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.domain.application.event.HarborPayload;
import io.choerodon.devops.domain.application.valueobject.ReplaceResult;
import io.choerodon.devops.infra.common.util.enums.ResourceType;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by Zenger on 2018/4/3.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_instances")
public class ApplicationInstanceController {
    private static final String ERROR_APPINSTANCE_QUERY = "error.appInstance.query";

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
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询应用部署")
    @CustomPageRequest
    @PostMapping(value = "/list_by_options")
    public ResponseEntity<Page<DevopsEnvPreviewInstanceDTO>> pageByOptions(
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
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
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
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
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
     * 根据实例id和deployment name获取更多部署详情(Json格式)
     *
     * @param projectId      项目id
     * @param appInstanceId  实例id
     * @param deploymentName deployment name
     * @return 部署详情
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多部署详情(Json格式)")
    @GetMapping(value = "/{appInstanceId}/deployment_detail_json")
    public ResponseEntity<InstanceControllerDetailDTO> getDeploymentDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "deployment_name") String deploymentName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return new ResponseEntity<>(applicationInstanceService.getInstanceResourceDetailJson(appInstanceId, deploymentName, ResourceType.DEPLOYMENT), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多daemonSet详情(Json格式)
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @param daemonSetName daemonSet name
     * @return daemonSet详情
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多daemonSet详情(Json格式)")
    @GetMapping(value = "/{appInstanceId}/daemon_set_detail_json")
    public ResponseEntity<InstanceControllerDetailDTO> getDaemonSetDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "daemon_set_name") String daemonSetName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return new ResponseEntity<>(applicationInstanceService.getInstanceResourceDetailJson(appInstanceId, daemonSetName, ResourceType.DAEMONSET), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多statefulSet详情(Json格式)
     *
     * @param projectId       项目id
     * @param appInstanceId   实例id
     * @param statefulSetName statefulSet name
     * @return statefulSet详情
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多statefulSet详情(Json格式)")
    @GetMapping(value = "/{appInstanceId}/stateful_set_detail_json")
    public ResponseEntity<InstanceControllerDetailDTO> getStatefulSetDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "stateful_set_name") String statefulSetName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return new ResponseEntity<>(applicationInstanceService.getInstanceResourceDetailJson(appInstanceId, statefulSetName, ResourceType.STATEFULSET), HttpStatus.OK);
    }

    /**
     * 根据实例id和deployment name获取更多部署详情(Yaml格式)
     *
     * @param projectId      项目id
     * @param appInstanceId  实例id
     * @param deploymentName deployment name
     * @return 部署详情
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多部署详情(Yaml格式)")
    @GetMapping(value = "/{appInstanceId}/deployment_detail_yaml")
    public ResponseEntity<InstanceControllerDetailDTO> getDeploymentDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "deployment_name") String deploymentName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return new ResponseEntity<>(applicationInstanceService.getInstanceResourceDetailYaml(appInstanceId, deploymentName, ResourceType.DEPLOYMENT), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多daemonSet详情(Yaml格式)
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @param daemonSetName daemonSet name
     * @return daemonSet详情
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多daemonSet详情(Yaml格式)")
    @GetMapping(value = "/{appInstanceId}/daemon_set_detail_yaml")
    public ResponseEntity<InstanceControllerDetailDTO> getDaemonSetDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "daemon_set_name") String daemonSetName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return new ResponseEntity<>(applicationInstanceService.getInstanceResourceDetailYaml(appInstanceId, daemonSetName, ResourceType.DAEMONSET), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多statefulSet详情(Yaml格式)
     *
     * @param projectId       项目id
     * @param appInstanceId   实例id
     * @param statefulSetName statefulSet name
     * @return statefulSet详情
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多statefulSet详情(Yaml格式)")
    @GetMapping(value = "/{appInstanceId}/stateful_set_detail_yaml")
    public ResponseEntity<InstanceControllerDetailDTO> getStatefulSetDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "stateful_set_name") String statefulSetName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId) {
        return new ResponseEntity<>(applicationInstanceService.getInstanceResourceDetailYaml(appInstanceId, statefulSetName, ResourceType.STATEFULSET), HttpStatus.OK);
    }

    /**
     * 获取升级 Value
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @param appVersionId  版本Id
     * @return ReplaceResult
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取升级 Value")
    @GetMapping(value = "/{appInstanceId}/appVersion/{appVersionId}/value")
    public ResponseEntity<ReplaceResult> queryUpgradeValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable Long appInstanceId,
            @ApiParam(value = "版本Id", required = true)
            @PathVariable Long appVersionId) {
        return Optional.ofNullable(applicationInstanceService.queryUpgradeValue(appInstanceId, appVersionId))
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
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
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
     * @param projectId     项目id
     * @param replaceResult 部署value
     * @return ReplaceResult
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
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
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
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
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PostMapping
    public ResponseEntity<ApplicationInstanceDTO> deploy(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署信息", required = true)
            @RequestBody ApplicationDeployDTO applicationDeployDTO) {
        return Optional.ofNullable(applicationInstanceService.createOrUpdate(applicationDeployDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.deploy"));
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
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
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
                .orElseThrow(() -> new CommonException(ERROR_APPINSTANCE_QUERY));
    }

    /**
     * 环境下某应用运行中或失败的实例
     *
     * @param projectId 项目id
     * @param appId     应用id
     * @param envId     环境id
     * @return list of AppInstanceCodeDTO
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "环境下某应用运行中或失败的实例")
    @GetMapping("/listByAppIdAndEnvId")
    public ResponseEntity<List<AppInstanceCodeDTO>> listByAppIdAndEnvId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID")
            @RequestParam Long envId,
            @ApiParam(value = "应用Id")
            @RequestParam Long appId) {
        return Optional.ofNullable(applicationInstanceService.listByAppIdAndEnvId(projectId, appId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APPINSTANCE_QUERY));
    }


    /**
     * 获取部署实例资源对象
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @return DevopsEnvResourceDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
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
     * 获取部署实例Event事件
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署实例Event事件")
    @GetMapping("/{app_instanceId}/events")
    public ResponseEntity<List<InstanceEventDTO>> listEvents(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "app_instanceId") Long appInstanceId) {
        return Optional.ofNullable(devopsEnvResourceService.listInstancePodEvent(appInstanceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.event.query"));
    }

    /**
     * 实例停止
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
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
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
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
     * 实例重新部署
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "实例重新部署")
    @PutMapping(value = "/{instanceId}/restart")
    public ResponseEntity restart(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable Long instanceId) {
        applicationInstanceService.instanceReStart(instanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 实例删除
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
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

    /**
     * 校验实例名唯一性
     *
     * @param projectId    项目id
     * @param instanceName 实例名
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "校验实例名唯一性")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @RequestParam(value = "instance_name") String instanceName) {
        applicationInstanceService.checkName(instanceName);
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
                    InitRoleCode.PROJECT_MEMBER})
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
                .orElseThrow(() -> new CommonException(ERROR_APPINSTANCE_QUERY));
    }

    /**
     * 获取部署时长报表
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param appIds    应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署时长报表")
    @PostMapping(value = "/env_commands/time")
    public ResponseEntity<DeployTimeDTO> listDeployTime(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "envId")
            @RequestParam(required = false) Long envId,
            @ApiParam(value = "appIds")
            @RequestBody(required = false) Long[] appIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(applicationInstanceService.listDeployTime(projectId, envId, appIds, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.deploy.time.get"));
    }


    /**
     * 获取部署次数报表
     *
     * @param projectId 项目id
     * @param envIds    环境id
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署次数报表")
    @PostMapping(value = "/env_commands/frequency")
    public ResponseEntity<DeployFrequencyDTO> listDeployFrequency(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "appId")
            @RequestParam(required = false) Long appId,
            @ApiParam(value = "envIds")
            @RequestBody(required = false) Long[] envIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(applicationInstanceService.listDeployFrequency(projectId, envIds, appId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.deploy.frequency.get"));
    }


    /**
     * 获取部署次数报表table
     *
     * @param projectId 项目id
     * @param envIds    环境id
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署次数报表table")
    @CustomPageRequest
    @PostMapping(value = "/env_commands/frequencyDetail")
    public ResponseEntity<Page<DeployDetailDTO>> pageDeployFrequencyDetail(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数") PageRequest pageRequest,
            @ApiParam(value = "appId")
            @RequestParam(required = false) Long appId,
            @ApiParam(value = "envIds")
            @RequestBody(required = false) Long[] envIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(applicationInstanceService.pageDeployFrequencyDetail(projectId, pageRequest, envIds, appId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.deploy.frequency.get"));
    }


    /**
     * 获取部署时长报表table
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param appIds    应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署时长报表table")
    @CustomPageRequest
    @PostMapping(value = "/env_commands/timeDetail")
    public ResponseEntity<Page<DeployDetailDTO>> pageDeployTimeDetail(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
                    PageRequest pageRequest,
            @ApiParam(value = "envId")
            @RequestParam(required = false) Long envId,
            @ApiParam(value = "appIds")
            @RequestBody(required = false) Long[] appIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(applicationInstanceService.pageDeployTimeDetail(projectId, pageRequest, appIds, envId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.deploy.time.get"));
    }

    /**
     * 部署自动化测试应用
     *
     * @param projectId            项目id
     * @param applicationDeployDTO 部署信息
     * @return ApplicationInstanceDTO
     */
    @ApiOperation(value = "部署自动化测试应用")
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PostMapping("/deploy_test_app")
    public void deployTestApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署信息", required = true)
            @RequestBody ApplicationDeployDTO applicationDeployDTO) {
        applicationInstanceService.deployTestApp(applicationDeployDTO);
    }

    /**
     * 操作pod的数量
     *
     * @param projectId      项目id
     * @param envId          环境id
     * @param deploymentName deploymentName
     * @param count          pod数量
     * @return ApplicationInstanceDTO
     */
    @ApiOperation(value = "操作pod的数量")
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PutMapping("/operate_pod_count")
    public void operatePodCount(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "deploymentName", required = true)
            @RequestParam String deploymentName,
            @ApiParam(value = "pod数量", required = true)
            @RequestParam Long count) {
        applicationInstanceService.operationPodCount(deploymentName, envId, count);
    }


    /**
     * 获取实例操作日志
     *
     * @param projectId     项目id
     * @param appInstanceId 实例id
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return List
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取实例操作日志")
    @CustomPageRequest
    @PostMapping(value = "/command_log/{appInstanceId}")
    public ResponseEntity<Page<AppInstanceCommandLogDTO>> listCommandLogs(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数") PageRequest pageRequest,
            @ApiParam(value = "实例 ID", required = true)
            @PathVariable Long appInstanceId,
            @ApiParam(value = "startTime")
            @RequestParam(required = false) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = false) Date endTime) {
        return Optional.ofNullable(applicationInstanceService.listAppInstanceCommand(pageRequest, appInstanceId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.deploy.log.get"));
    }
}
