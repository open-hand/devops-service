package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.swagger.annotation.CustomPageRequest;
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
@RequestMapping(value = "/v1/projects/{project_id}/app_service_instances")
public class AppServiceInstanceController {

    private static final String ERROR_APP_INSTANCE_QUERY = "error.instance.query";

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;


    /**
     * 根据实例id获取实例信息
     *
     * @param projectId     项目id
     * @param instanceId 实例id
     * @return 实例信息
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取实例信息")
    @GetMapping(value = "/{instance_id}")
    public ResponseEntity<AppServiceInstanceInfoVO> queryInstanceInformationById(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return Optional.ofNullable(appServiceInstanceService.queryInfoById(instanceId))
                .map(info -> new ResponseEntity<>(info, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.query.instance.by.id"));
    }


    /**
     * 分页查询环境下实例信息（基本信息）
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param envId       环境id
     * @param params      搜索参数
     * @return page of AppInstanceInfoVO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询环境下实例信息（基本信息）")
    @CustomPageRequest
    @PostMapping(value = "/info/page_by_options")
    public ResponseEntity<PageInfo<AppServiceInstanceInfoVO>> pageInstanceInfoByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiIgnore
            @ApiParam(value =  "分页参数") PageRequest pageRequest,
            @ApiParam(value = "环境ID")
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return new ResponseEntity<>(
                appServiceInstanceService.pageInstanceInfoByOptions(projectId, envId, pageRequest, params), HttpStatus.OK);
    }


    /**
     * 分页查询服务部署
     *
     * @param projectId    项目id
     * @param pageRequest  分页参数
     * @param envId        环境id
     * @param versionId    版本id
     * @param appServiceId 服务id
     * @param params       搜索参数
     * @return page of DevopsEnvPreviewInstanceVO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询服务部署")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<PageInfo<DevopsEnvPreviewInstanceVO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiIgnore
            @ApiParam(value = "分页参数") PageRequest pageRequest,
            @ApiParam(value = "环境ID")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "版本ID")
            @RequestParam(value = "version_id", required = false) Long versionId,
            @ApiParam(value = "服务ID")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "实例ID")
            @RequestParam(value = "instance_id", required = false) Long instanceId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(appServiceInstanceService.pageByOptions(
                projectId, pageRequest, envId, versionId, appServiceId, instanceId, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.version.query"));
    }

    /**
     * 查询部署总览
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @return page of ApplicationInstancesVO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询部署总览")
    @GetMapping(value = "/list_application_instance_overView")
    public ResponseEntity<List<AppServiceInstanceOverViewVO>> listApplicationInstanceOverView(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务ID")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId) {
        return Optional.ofNullable(appServiceInstanceService.listApplicationInstanceOverView(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.version.query"));
    }

    /**
     * 获取实例上次部署配置
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return string
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取实例上次部署配置")
    @GetMapping(value = "/{instance_Id}/last_deploy_value")
    public ResponseEntity<InstanceValueVO> queryLastDeployValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_Id") Long instanceId) {
        return Optional.ofNullable(appServiceInstanceService.queryLastDeployValue(instanceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.instance.value.get"));
    }


    /**
     * 根据实例id和deployment name获取更多部署详情(Json格式)
     *
     * @param projectId      项目id
     * @param instanceId  实例id
     * @param deploymentName deployment name
     * @return 部署详情
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多部署详情(Json格式)")
    @GetMapping(value = "/{instance_id}/deployment_detail_json")
    public ResponseEntity<InstanceControllerDetailVO> getDeploymentDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "deployment_name") String deploymentName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.queryInstanceResourceDetailJson(instanceId, deploymentName, ResourceType.DEPLOYMENT), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多daemonSet详情(Json格式)
     *
     * @param projectId     项目id
     * @param instanceId 实例id
     * @param daemonSetName daemonSet name
     * @return daemonSet详情
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多daemonSet详情(Json格式)")
    @GetMapping(value = "/{instance_id}/daemon_set_detail_json")
    public ResponseEntity<InstanceControllerDetailVO> getDaemonSetDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "daemon_set_name") String daemonSetName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.queryInstanceResourceDetailJson(instanceId, daemonSetName, ResourceType.DAEMONSET), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多statefulSet详情(Json格式)
     *
     * @param projectId       项目id
     * @param instanceId   实例id
     * @param statefulSetName statefulSet name
     * @return statefulSet详情
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多statefulSet详情(Json格式)")
    @GetMapping(value = "/{instance_id}/stateful_set_detail_json")
    public ResponseEntity<InstanceControllerDetailVO> getStatefulSetDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "stateful_set_name") String statefulSetName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.queryInstanceResourceDetailJson(instanceId, statefulSetName, ResourceType.STATEFULSET), HttpStatus.OK);
    }

    /**
     * 根据实例id和deployment name获取更多部署详情(Yaml格式)
     *
     * @param projectId      项目id
     * @param instanceId  实例id
     * @param deploymentName deployment name
     * @return 部署详情
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多部署详情(Yaml格式)")
    @GetMapping(value = "/{instance_id}/deployment_detail_yaml")
    public ResponseEntity<InstanceControllerDetailVO> getDeploymentDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "deployment_name") String deploymentName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.getInstanceResourceDetailYaml(instanceId, deploymentName, ResourceType.DEPLOYMENT), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多daemonSet详情(Yaml格式)
     *
     * @param projectId     项目id
     * @param instanceId 实例id
     * @param daemonSetName daemonSet name
     * @return daemonSet详情
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多daemonSet详情(Yaml格式)")
    @GetMapping(value = "/{instance_id}/daemon_set_detail_yaml")
    public ResponseEntity<InstanceControllerDetailVO> getDaemonSetDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "daemon_set_name") String daemonSetName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.getInstanceResourceDetailYaml(instanceId, daemonSetName, ResourceType.DAEMONSET), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多statefulSet详情(Yaml格式)
     *
     * @param projectId       项目id
     * @param instanceId   实例id
     * @param statefulSetName statefulSet name
     * @return statefulSet详情
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多statefulSet详情(Yaml格式)")
    @GetMapping(value = "/{instance_id}/stateful_set_detail_yaml")
    public ResponseEntity<InstanceControllerDetailVO> getStatefulSetDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "stateful_set_name") String statefulSetName,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.getInstanceResourceDetailYaml(instanceId, statefulSetName, ResourceType.STATEFULSET), HttpStatus.OK);
    }

    /**
     * 获取升级Value
     *
     * @param projectId     项目id
     * @param instanceId 实例id
     * @param version_id  版本Id
     * @return InstanceValueVO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取升级Value")
    @GetMapping(value = "/{instance_id}/appServiceService/{version_id}/upgrade_value")
    public ResponseEntity<InstanceValueVO> queryUpgradeValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId,
            @ApiParam(value = "版本Id", required = true)
            @PathVariable(value = "version_id") Long version_id) {
        return Optional.ofNullable(appServiceInstanceService.queryUpgradeValue(instanceId, version_id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.instance.value.get"));
    }

    /**
     * 查询服务部署时value
     *
     * @param projectId    项目id
     * @param type         部署类型
     * @param instanceId   实例Id
     * @param versionId 版本id
     * @return InstanceValueVO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询服务部署时value")
    @GetMapping("/deploy_value")
    public ResponseEntity<InstanceValueVO> queryDeployValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "type", required = true)
            @RequestParam String type,
            @ApiParam(value = "实例ID")
            @RequestParam(value = "instance_id", required = false) Long instanceId,
            @ApiParam(value = "版本ID")
            @RequestParam(value = "version_id") Long versionId) {
        return Optional.ofNullable(appServiceInstanceService.queryDeployValue(type, instanceId, versionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.values.query"));
    }


    /**
     * @param projectId       项目id
     * @param instanceValueVO 部署value
     * @return InstanceValueVO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询预览value")
    @PostMapping("/preview_value")
    public ResponseEntity<InstanceValueVO> previewValues(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam
            @RequestBody InstanceValueVO instanceValueVO,
            @ApiParam(value = "版本ID", required = true)
            @RequestParam Long versionId) {
        return Optional.ofNullable(appServiceInstanceService.queryPreviewValues(instanceValueVO, versionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.values.query"));
    }

    /**
     * 校验values
     *
     * @param instanceValueVO values对象
     * @return List
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "校验values")
    @PostMapping("/value_format")
    public ResponseEntity<List<ErrorLineVO>> formatValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "value", required = true)
            @RequestBody InstanceValueVO instanceValueVO) {
        return new ResponseEntity<>(appServiceInstanceService.formatValue(instanceValueVO), HttpStatus.OK);
    }

    /**
     * 部署服务
     *
     * @param projectId           项目id
     * @param appServiceDeployVO 部署信息
     * @return ApplicationInstanceVO
     */
    @ApiOperation(value = "部署服务")
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PostMapping
    public ResponseEntity<AppServiceInstanceVO> deploy(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署信息", required = true)
            @RequestBody AppServiceDeployVO appServiceDeployVO) {
        return Optional.ofNullable(appServiceInstanceService.createOrUpdate(appServiceDeployVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.deploy"));
    }

    /**
     * 查询运行中的实例
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param versionId 服务版本id
     * @param envId        环境id
     * @return baseList of AppInstanceCodeDTO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询运行中的实例")
    @GetMapping("/list_running_instance")
    public ResponseEntity<List<RunningInstanceVO>> listRunningInstance(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "服务Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "服务版本 ID")
            @RequestParam(value = "version_id", required = false) Long versionId) {
        return Optional.ofNullable(appServiceInstanceService.listRunningInstance(projectId, appServiceId, versionId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APP_INSTANCE_QUERY));
    }

    /**
     * 环境下某服务运行中或失败的实例
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param envId        环境id
     * @return baseList of RunningInstanceVO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "环境下某服务运行中或失败的实例")
    @GetMapping("/list_running_and_failed")
    public ResponseEntity<List<RunningInstanceVO>> listByAppServiceIdAndEnvId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID")
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "服务 Id")
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(appServiceInstanceService.listByAppIdAndEnvId(projectId, appServiceId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APP_INSTANCE_QUERY));
    }


    /**
     * 获取部署实例release相关对象
     *
     * @param projectId     项目id
     * @param instance_id 实例id
     * @return DevopsEnvResourceDTO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署实例release相关对象")
    @GetMapping("/{instance_id}/resources")
    public ResponseEntity<DevopsEnvResourceVO> listResourcesInHelmRelease(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instance_id) {
        return Optional.ofNullable(appServiceInstanceService.listResourcesInHelmRelease(instance_id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.resource.query"));
    }

    /**
     * 获取部署实例Event事件
     *
     * @param projectId     项目id
     * @param instance_id 实例id
     * @return List
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署实例Event事件")
    @GetMapping("/{instance_id}/events")
    public ResponseEntity<List<InstanceEventVO>> listEvents(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instance_id) {
        return Optional.ofNullable(devopsEnvResourceService.listInstancePodEvent(instance_id))
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
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "实例停止")
    @PutMapping(value = "/{instance_id}/stop")
    public ResponseEntity stop(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        appServiceInstanceService.stopInstance(instanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 实例重启
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "实例重启")
    @PutMapping(value = "/{instance_id}/start")
    public ResponseEntity start(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable("instance_id") Long instanceId) {
        appServiceInstanceService.startInstance(instanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 实例重新部署
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "实例重新部署")
    @PutMapping(value = "/{instance_id}/restart")
    public ResponseEntity restart(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        appServiceInstanceService.restartInstance(instanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 实例删除
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "实例删除")
    @DeleteMapping(value = "/{instance_id}/delete")
    public ResponseEntity delete(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        appServiceInstanceService.deleteInstance(instanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 校验实例名唯一性
     *
     * @param projectId    项目id
     * @param instanceName 实例名
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "校验实例名唯一性")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @RequestParam(value = "instance_name") String instanceName,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId) {
        appServiceInstanceService.checkName(instanceName, envId);
    }

    /**
     * 环境总览实例查询
     *
     * @param projectId 项目id
     * @param envId     环境Id
     * @param params    搜索参数
     * @return DevopsEnvPreviewDTO
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "环境总览实例查询")
    @PostMapping(value = "/{env_id}/listByEnv")
    public ResponseEntity<DevopsEnvPreviewVO> listByEnv(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "envId", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(appServiceInstanceService.listByEnv(projectId, envId, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APP_INSTANCE_QUERY));
    }

    /**
     * 获取部署时长报表
     *
     * @param projectId     项目id
     * @param envId         环境id
     * @param appServiceIds 服务id
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return List
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署时长报表")
    @PostMapping(value = "/env_commands/time")
    public ResponseEntity<DeployTimeVO> listDeployTimeReport(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "envId")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "appServiceIds")
            @RequestBody(required = false) Long[] appServiceIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(appServiceInstanceService.listDeployTime(projectId, envId, appServiceIds, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.deploy.time.get"));
    }

    /**
     * 获取部署次数报表
     *
     * @param projectId    项目id
     * @param envIds       环境id
     * @param appServiceId 服务id
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return List
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署次数报表")
    @PostMapping(value = "/env_commands/frequency")
    public ResponseEntity<DeployFrequencyVO> listDeployFrequencyReport(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "appServiceId")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "envIds")
            @RequestBody(required = false) Long[] envIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(appServiceInstanceService.listDeployFrequency(projectId, envIds, appServiceId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.deploy.frequency.get"));
    }


    /**
     * 分页获取部署次数列表
     *
     * @param projectId    项目id
     * @param envIds       环境id
     * @param appServiceId 服务id
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return List
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页获取部署次数列表")
    @CustomPageRequest
    @PostMapping(value = "/env_commands/frequencyTable")
    public ResponseEntity<PageInfo<DeployDetailTableVO>> pageDeployFrequencyDetailTable(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数") PageRequest pageRequest,
            @ApiParam(value = "appServiceId")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "envIds")
            @RequestBody(required = false) Long[] envIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(appServiceInstanceService.pageDeployFrequencyTable(projectId, pageRequest, envIds, appServiceId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.deploy.frequency.get"));
    }


    /**
     * 分页获取部署时长列表
     *
     * @param projectId     项目id
     * @param envId         环境id
     * @param appServiceIds 服务id
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return PageInfo
     */
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页获取部署时长列表")
    @CustomPageRequest
    @PostMapping(value = "/env_commands/timeTable")
    public ResponseEntity<PageInfo<DeployDetailTableVO>> pageDeployTimeTable(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
                    PageRequest pageRequest,
            @ApiParam(value = "envId")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "appServiceIds")
            @RequestBody(required = false) Long[] appServiceIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(appServiceInstanceService.pageDeployTimeTable(projectId, pageRequest, appServiceIds, envId, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.deploy.time.get"));
    }

    /**
     * 部署自动化测试服务
     *
     * @param projectId           项目id
     * @param appServiceDeployVO 部署信息
     * @return ApplicationInstanceVO
     */
    @ApiOperation(value = "部署自动化测试服务")
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PostMapping("/deploy_test_app")
    public void deployTestApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署信息", required = true)
            @RequestBody AppServiceDeployVO appServiceDeployVO) {
        appServiceInstanceService.deployTestApp(appServiceDeployVO);
    }

    /**
     * 操作pod的数量
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param name      deploymentName
     * @param count     pod数量
     * @return ApplicationInstanceVO
     */
    @ApiOperation(value = "操作pod的数量")
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PutMapping("/operate_pod_count")
    public void operatePodCount(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "name", required = true)
            @RequestParam String name,
            @ApiParam(value = "pod数量", required = true)
            @RequestParam Long count) {
        appServiceInstanceService.operationPodCount(name, envId, count);
    }

    /**
     * 部署远程服务市场服务
     *
     * @param projectId
     * @param appRemoteDeployDTO
     * @return
     */
    @ApiOperation(value = "部署远程服务市场服务")
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PostMapping(value = "/deploy_remote_app")
    public ResponseEntity<AppServiceInstanceVO> deployRemoteApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署信息", required = true)
            @RequestBody AppServiceRemoteDeployVO appRemoteDeployDTO) {
        return Optional.ofNullable(appServiceInstanceService.deployRemoteApp(projectId, appRemoteDeployDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.remote.deploy"));
    }


    /**
     * 根据实例commandId查询实例信息
     *
     * @param projectId
     * @param commandId
     * @return
     */
    @ApiOperation(value = "根据实例commandId查询实例信息")
    @Permission(type = io.choerodon.base.enums.ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/query_by_command/{command_id}")
    public ResponseEntity<AppServiceInstanceRepVO> deployRemoteApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "commandId", required = true)
            @PathVariable(value = "command_id") Long commandId) {
        return Optional.ofNullable(appServiceInstanceService.queryByCommandId(commandId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.instance.get"));
    }

}
