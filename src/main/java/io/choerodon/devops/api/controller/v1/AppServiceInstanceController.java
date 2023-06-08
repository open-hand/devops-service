package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.AppServiceInstanceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.application.ApplicationInstanceInfoVO;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.infra.config.SwaggerApiConfig;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.KeyDecryptHelper;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;


/**
 * Created by Zenger on 2018/4/3.
 */
@Api(tags = SwaggerApiConfig.APP_SERVICE_INSTANCE)
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_service_instances")
public class AppServiceInstanceController {

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private AppServiceInstanceValidator appServiceInstanceValidator;

    /**
     * 根据实例id获取实例信息
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return 实例信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取实例信息")
    @GetMapping(value = "/{instance_id}")
    public ResponseEntity<AppServiceInstanceInfoVO> queryInstanceInformationById(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.queryInfoById(projectId, instanceId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("部署应用市场的服务")
    @PostMapping("/market/instances")
    public ResponseEntity<AppServiceInstanceVO> deployMarketService(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody @Valid MarketInstanceCreationRequestVO marketInstanceCreationRequestVO) {
        marketInstanceCreationRequestVO.setCommandType(CommandType.CREATE.getType());
        marketInstanceCreationRequestVO.setSource(AppSourceType.MARKET.getValue());
        marketInstanceCreationRequestVO.setOperationType(OperationTypeEnum.CREATE_APP.value());
        return ResponseEntity.ok(appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO, true));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("更新应用市场的服务实例")
    @PutMapping("/market/instances/{instance_id}")
    public ResponseEntity<AppServiceInstanceVO> updateMarketServiceInstance(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "实例id", required = true)
            @PathVariable(value = "instance_id") Long instanceId,
            @RequestBody MarketInstanceCreationRequestVO marketInstanceCreationRequestVO) {
        marketInstanceCreationRequestVO.setCommandType(CommandType.UPDATE.getType());
        marketInstanceCreationRequestVO.setInstanceId(instanceId);
        marketInstanceCreationRequestVO.setSource(AppSourceType.MARKET.getValue());
        return ResponseEntity.ok(appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO, true));
    }

    /**
     * 分页查询环境下实例信息（基本信息）
     *
     * @param projectId 项目id
     * @param pageable  分页参数
     * @param envId     环境id
     * @param params    搜索参数
     * @return page of AppInstanceInfoVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询环境下实例信息（基本信息）")
    @CustomPageRequest
    @PostMapping(value = "/info/page_by_options")
    public ResponseEntity<Page<AppServiceInstanceInfoVO>> pageInstanceInfoByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiParam(value = "分页参数") PageRequest pageable,
            @Encrypt
            @ApiParam(value = "环境ID")
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return new ResponseEntity<>(
                appServiceInstanceService.pageInstanceInfoByOptions(projectId, envId, pageable, params), HttpStatus.OK);
    }

    /**
     * 获取实例上次部署配置
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return string
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取实例上次部署配置")
    @GetMapping(value = "/{instance_Id}/last_deploy_value")
    public ResponseEntity<InstanceValueVO> queryLastDeployValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_Id") Long instanceId) {
        return ResponseEntity.ok(appServiceInstanceService.queryLastDeployValue(instanceId));
    }


    /**
     * 根据实例id和deployment name获取更多部署详情(Json格式)
     *
     * @param projectId      项目id
     * @param instanceId     实例id
     * @param deploymentName deployment name
     * @return 部署详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多部署详情(Json格式)")
    @GetMapping(value = "/{instance_id}/deployment_detail_json")
    public ResponseEntity<InstanceControllerDetailVO> getDeploymentDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "deployment_name") String deploymentName,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.queryInstanceResourceDetailJson(instanceId, deploymentName, ResourceType.DEPLOYMENT), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多daemonSet详情(Json格式)
     *
     * @param projectId     项目id
     * @param instanceId    实例id
     * @param daemonSetName daemonSet name
     * @return daemonSet详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多daemonSet详情(Json格式)")
    @GetMapping(value = "/{instance_id}/daemon_set_detail_json")
    public ResponseEntity<InstanceControllerDetailVO> getDaemonSetDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "daemon_set_name") String daemonSetName,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.queryInstanceResourceDetailJson(instanceId, daemonSetName, ResourceType.DAEMONSET), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多statefulSet详情(Json格式)
     *
     * @param projectId       项目id
     * @param instanceId      实例id
     * @param statefulSetName statefulSet name
     * @return statefulSet详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多statefulSet详情(Json格式)")
    @GetMapping(value = "/{instance_id}/stateful_set_detail_json")
    public ResponseEntity<InstanceControllerDetailVO> getStatefulSetDetailsJsonByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "stateful_set_name") String statefulSetName,
            @ApiParam(value = "部署ID", required = true)
            @Encrypt
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.queryInstanceResourceDetailJson(instanceId, statefulSetName, ResourceType.STATEFULSET), HttpStatus.OK);
    }

    /**
     * 根据实例id和deployment name获取更多部署详情(Yaml格式)
     *
     * @param projectId      项目id
     * @param instanceId     实例id
     * @param deploymentName deployment name
     * @return 部署详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多部署详情(Yaml格式)")
    @GetMapping(value = "/{instance_id}/deployment_detail_yaml")
    public ResponseEntity<InstanceControllerDetailVO> getDeploymentDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "deployment_name") String deploymentName,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.getInstanceResourceDetailYaml(instanceId, deploymentName, ResourceType.DEPLOYMENT), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多daemonSet详情(Yaml格式)
     *
     * @param projectId     项目id
     * @param instanceId    实例id
     * @param daemonSetName daemonSet name
     * @return daemonSet详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多daemonSet详情(Yaml格式)")
    @GetMapping(value = "/{instance_id}/daemon_set_detail_yaml")
    public ResponseEntity<InstanceControllerDetailVO> getDaemonSetDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "daemon_set_name") String daemonSetName,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.getInstanceResourceDetailYaml(instanceId, daemonSetName, ResourceType.DAEMONSET), HttpStatus.OK);
    }

    /**
     * 根据实例id获取更多statefulSet详情(Yaml格式)
     *
     * @param projectId       项目id
     * @param instanceId      实例id
     * @param statefulSetName statefulSet name
     * @return statefulSet详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据实例id获取更多statefulSet详情(Yaml格式)")
    @GetMapping(value = "/{instance_id}/stateful_set_detail_yaml")
    public ResponseEntity<InstanceControllerDetailVO> getStatefulSetDetailsYamlByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署名称", required = true)
            @RequestParam(value = "stateful_set_name") String statefulSetName,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return new ResponseEntity<>(appServiceInstanceService.getInstanceResourceDetailYaml(instanceId, statefulSetName, ResourceType.STATEFULSET), HttpStatus.OK);
    }

    /**
     * 获取当前实例升级到特定版本的Values
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @param versionId  版本Id
     * @return InstanceValueVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取当前实例升级到特定版本的Values")
    @GetMapping(value = "/{instance_id}/appServiceVersion/{version_id}/upgrade_value")
    public ResponseEntity<InstanceValueVO> queryUpgradeValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId,
            @Encrypt
            @ApiParam(value = "版本Id", required = true)
            @PathVariable(value = "version_id") Long versionId) {
        return ResponseEntity.ok(appServiceInstanceService.queryUpgradeValue(instanceId, versionId));
    }

    /**
     * 获取当前实例生效的Values
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return InstanceValueVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取当前实例升级到特定版本的Values")
    @GetMapping(value = "/{instance_id}/values")
    public ResponseEntity<InstanceValueVO> queryValues(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return ResponseEntity.ok(appServiceInstanceService.queryValues(instanceId));
    }


    /**
     * 获取当前实例升级到特定版本的Values
     *
     * @param projectId            项目id
     * @param instanceId           实例id
     * @param marketDeployObjectId 版本Id
     * @return InstanceValueVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取当前市场实例升级到特定版本的Values")
    @GetMapping(value = "/{instance_id}/upgrade_value")
    public ResponseEntity<InstanceValueVO> queryUpgradeValueForMarketInstance(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId,
            @Encrypt
            @ApiParam(value = "市场发布包Id", required = true)
            @RequestParam(value = "market_deploy_object_id") Long marketDeployObjectId) {
        return ResponseEntity.ok(appServiceInstanceService.queryUpgradeValueForMarketInstance(projectId, instanceId, marketDeployObjectId));
    }

    /**
     * 获取当前实例升级到特定版本的Values
     *
     * @param projectId            项目id
     * @param instanceId           实例id
     * @param marketDeployObjectId 版本Id
     * @return InstanceValueVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取当前市场实例升级到特定版本的Values")
    @GetMapping(value = "/{instance_id}/market_value")
    public ResponseEntity<InstanceValueVO> queryValueForMarketInstance(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "部署ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId,
            @Encrypt
            @ApiParam(value = "市场发布包Id", required = true)
            @RequestParam(value = "market_deploy_object_id") Long marketDeployObjectId) {
        return ResponseEntity.ok(appServiceInstanceService.queryValueForMarketInstance(projectId, instanceId, marketDeployObjectId));
    }

    /**
     * 查询服务部署时value
     *
     * @param projectId  项目id
     * @param type       部署类型
     * @param instanceId 实例Id
     * @param versionId  版本id
     * @return InstanceValueVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询服务部署时value")
    @GetMapping("/deploy_value")
    public ResponseEntity<InstanceValueVO> queryDeployValue(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "type", required = true)
            @RequestParam String type,
            @Encrypt
            @ApiParam(value = "实例ID")
            @RequestParam(value = "instance_id", required = false) Long instanceId,
            @Encrypt
            @ApiParam(value = "版本ID")
            @RequestParam(value = "version_id") Long versionId) {
        return ResponseEntity.ok(appServiceInstanceService.queryDeployValue(type, instanceId, versionId));
    }


    /**
     * @param projectId       项目id
     * @param instanceValueVO 部署value
     * @return InstanceValueVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询预览value")
    @PostMapping("/preview_value")
    public ResponseEntity<InstanceValueVO> previewValues(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam
            @RequestBody InstanceValueVO instanceValueVO,
            @Encrypt
            @ApiParam(value = "版本ID", required = true)
            @RequestParam Long versionId) {
        return ResponseEntity.ok(appServiceInstanceService.queryPreviewValues(instanceValueVO, versionId));
    }

    /**
     * 校验values
     *
     * @param instanceValueVO values对象
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
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
     * @param projectId          项目id
     * @param appServiceDeployVO 部署信息
     * @return ApplicationInstanceVO
     */
    @ApiOperation(value = "部署服务")
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PostMapping
    public ResponseEntity<AppServiceInstanceVO> deploy(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署信息", required = true)
            @RequestBody @Valid AppServiceDeployVO appServiceDeployVO) {
        appServiceDeployVO.setType("create");
        return ResponseEntity.ok(appServiceInstanceService.createOrUpdate(projectId, appServiceDeployVO, DeployType.MANUAL));
    }

    /**
     * 更新服务
     *
     * @param projectId                项目id
     * @param appServiceDeployUpdateVO 更新信息
     * @return ApplicationInstanceVO
     */
    @ApiOperation(value = "更新实例")
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PutMapping
    public ResponseEntity<AppServiceInstanceVO> update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "更新信息", required = true)
            @RequestBody @Valid AppServiceDeployUpdateVO appServiceDeployUpdateVO) {
        appServiceDeployUpdateVO.setType("update");
        return ResponseEntity.ok(appServiceInstanceService.createOrUpdate(projectId, ConvertUtils.convertObject(appServiceDeployUpdateVO, AppServiceDeployVO.class), DeployType.MANUAL));
    }

    /**
     * 查询运行中的实例
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param versionId    服务版本id
     * @param envId        环境id
     * @return baseList of AppInstanceCodeDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询运行中的实例")
    @GetMapping("/list_running_instance")
    public ResponseEntity<List<RunningInstanceVO>> listRunningInstance(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境 ID")
            @RequestParam(value = "env_id", required = false) Long envId,
            @Encrypt
            @ApiParam(value = "服务Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @Encrypt
            @ApiParam(value = "服务版本 ID")
            @RequestParam(value = "version_id", required = false) Long versionId) {
        return ResponseEntity.ok(appServiceInstanceService.listRunningInstance(projectId, appServiceId, versionId, envId));
    }

    /**
     * 环境下某服务运行中或失败的实例
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param envId        环境id
     * @return baseList of RunningInstanceVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "环境下某服务运行中或失败的实例")
    @GetMapping("/list_running_and_failed")
    public ResponseEntity<List<RunningInstanceVO>> listByAppServiceIdAndEnvId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境 ID")
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "服务 Id")
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(appServiceInstanceService.listByAppIdAndEnvId(projectId, appServiceId, envId));
    }


    /**
     * 获取部署实例release相关对象
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return DevopsEnvResourceDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署实例release中的各种资源")
    @GetMapping("/{instance_id}/resources")
    public ResponseEntity<DevopsEnvResourceVO> listResourcesInHelmRelease(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return ResponseEntity.ok(appServiceInstanceService.listResourcesInHelmRelease(instanceId));
    }

    /**
     * 获取部署实例Event事件
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署实例Event事件")
    @GetMapping("/{instance_id}/events")
    public ResponseEntity<List<InstanceEventVO>> listEvents(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        return ResponseEntity.ok(devopsEnvResourceService.listInstancePodEvent(instanceId));
    }

    /**
     * 实例停止
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "实例停止")
    @PutMapping(value = "/{instance_id}/stop")
    public ResponseEntity<Void> stop(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        appServiceInstanceService.stopInstance(projectId, instanceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 实例重启
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "实例重启")
    @PutMapping(value = "/{instance_id}/start")
    public ResponseEntity<Void> start(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable("instance_id") Long instanceId) {
        appServiceInstanceService.startInstance(projectId, instanceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 实例重新部署
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "实例重新部署")
    @PutMapping(value = "/{instance_id}/restart")
    public ResponseEntity<Void> restart(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        appServiceInstanceService.restartInstance(projectId, instanceId, DeployType.MANUAL, true);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 实例删除
     *
     * @param projectId  项目id
     * @param instanceId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "实例删除")
    @DeleteMapping(value = "/{instance_id}/delete")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "instance_id") Long instanceId) {
        appServiceInstanceService.deleteInstance(projectId, instanceId, false);
        return ResponseEntity.noContent().build();
    }

    /**
     * 校验实例名唯一性
     *
     * @param projectId    项目id
     * @param instanceName 实例名
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验实例名唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @RequestParam(value = "instance_name") String instanceName,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return ResponseEntity.ok(appServiceInstanceService.isNameValid(instanceName, envId));
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
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取部署时长报表")
    @PostMapping(value = "/env_commands/time")
    public ResponseEntity<DeployTimeVO> listDeployTimeReport(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "envId")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "appServiceIds")
            @RequestBody(required = false) String[] appServiceIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return ResponseEntity.ok(appServiceInstanceService.listDeployTime(projectId, envId, KeyDecryptHelper.decryptIdArray(appServiceIds), startTime, endTime));
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
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "获取部署次数报表")
    @PostMapping(value = "/env_commands/frequency")
    public ResponseEntity<DeployFrequencyVO> listDeployFrequencyReport(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "appServiceId")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "envIds")
            @RequestBody(required = false) String[] envIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return ResponseEntity.ok(appServiceInstanceService.listDeployFrequency(projectId, KeyDecryptHelper.decryptIdArray(envIds), appServiceId, startTime, endTime));
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
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页获取部署次数列表")
    @CustomPageRequest
    @PostMapping(value = "/env_commands/frequencyTable")
    public ResponseEntity<Page<DeployDetailTableVO>> pageDeployFrequencyDetailTable(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数") PageRequest pageable,
            @Encrypt
            @ApiParam(value = "appServiceId")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "envIds")
            @RequestBody(required = false) String[] envIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return ResponseEntity.ok(appServiceInstanceService.pageDeployFrequencyTable(projectId, pageable, KeyDecryptHelper.decryptIdArray(envIds), appServiceId, startTime, endTime));
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
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页获取部署时长列表")
    @CustomPageRequest
    @PostMapping(value = "/env_commands/timeTable")
    public ResponseEntity<Page<DeployDetailTableVO>> pageDeployTimeTable(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数") PageRequest pageable,
            @Encrypt
            @ApiParam(value = "envId")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "appServiceIds")
            @RequestBody(required = false) String[] appServiceIds,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return ResponseEntity.ok(appServiceInstanceService.pageDeployTimeTable(projectId, pageable, KeyDecryptHelper.decryptIdArray(appServiceIds), envId, startTime, endTime));
    }

    /**
     * 操作pod的数量
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param name      deploymentName
     * @param count     pod数量
     */
    @ApiOperation(value = "操作pod的数量")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/operate_pod_count")
    public void operatePodCount(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例id", required = true)
            @Encrypt @RequestParam(value = "instanceId",required = false) Long instanceId,
            @ApiParam(value = "环境id", required = true)
            @Encrypt
            @RequestParam Long envId,
            @ApiParam(value = "kind", required = true)
            @RequestParam String kind,
            @ApiParam(value = "name", required = true)
            @RequestParam String name,
            @ApiParam(value = "pod数量", required = true)
            @RequestParam Long count,
            @ApiParam(value = "是否为操作工作负载pod", required = false)
            @RequestParam(value = "workload", defaultValue = "false", required = false) boolean workload) {
        appServiceInstanceService.operationPodCount(projectId, kind, instanceId, name, envId, count, workload);
    }


    /**
     * 根据实例commandId查询实例信息
     *
     * @param projectId 项目id
     * @param commandId 实例command Id
     * @return 实例信息
     */
    @ApiOperation(value = "根据实例commandId查询实例信息")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping(value = "/query_by_command/{command_id}")
    public ResponseEntity<AppServiceInstanceRepVO> deployRemoteApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "commandId", required = true)
            @PathVariable(value = "command_id") Long commandId) {
        return ResponseEntity.ok(appServiceInstanceService.queryByCommandId(commandId));
    }


    @ApiOperation("计算环境下实例的数量")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/count_by_options")
    public ResponseEntity<Integer> countByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam("env_id") Long envId,
            @ApiParam(value = "实例状态, 不填是查全部", required = false)
            @RequestParam(required = false) String status,
            @Encrypt
            @ApiParam(value = "应用服务id", required = false)
            @RequestParam(value = "app_service_id", required = false) Long appServiceId) {
        return ResponseEntity.ok(appServiceInstanceService.countByOptions(envId, status, appServiceId));
    }

    @ApiOperation("根据批量部署的部署纪录id查询对应的实例")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/query_by_deploy_record_id")
    public ResponseEntity<List<AppServiceInstanceForRecordVO>> queryByBatchDeployRecordId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "批量部署的部署纪录id", required = true)
            @RequestParam(value = "record_id") Long recordId) {
        return new ResponseEntity<>(devopsDeployRecordService.queryByBatchDeployRecordId(recordId), HttpStatus.OK);
    }

    @ApiOperation(value = "批量部署服务")
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PostMapping("/batch_deployment")
    public ResponseEntity<List<AppServiceInstanceVO>> batchDeployment(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "批量部署信息数组", required = true)
            @RequestBody List<AppServiceDeployVO> appServiceDeployVOs) {
        // 校验参数正确性
        appServiceInstanceValidator.validateBatchDeployment(appServiceDeployVOs);
        return new ResponseEntity<>(appServiceInstanceService.batchDeployment(projectId, appServiceDeployVOs), HttpStatus.OK);
    }

    @ApiOperation("查询服务下在环境下的实例列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/list_by_service_and_env")
    public ResponseEntity<List<ApplicationInstanceInfoVO>> listByServiceAndEnv(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务ID", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return ResponseEntity.ok(appServiceInstanceService.listByServiceAndEnv(projectId, appServiceId, envId, true));
    }

    @ApiOperation("删除集群中对应的job资源，以停止helm hook操作")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{instance_id}/delete_job")
    public ResponseEntity<Void> deleteHelmHookJob(@ApiParam(value = "项目ID", required = true)
                                                  @PathVariable(value = "project_id") Long projectId,
                                                  @ApiParam(value = "应用实例id", required = true)
                                                  @Encrypt @PathVariable(value = "instance_id") Long instanceId,
                                                  @ApiParam(value = "操作id", required = true)
                                                  @Encrypt @RequestParam(value = "command_id") Long commandId,
                                                  @ApiParam(value = "环境id", required = true)
                                                  @Encrypt @RequestParam(value = "env_id") Long envId,
                                                  @ApiParam(value = "job名称", required = true)
                                                  @RequestParam(value = "job_name") String jobName) {
        appServiceInstanceService.deleteHelmHookJob(projectId, instanceId, envId, commandId, jobName);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "同步values到实例部署")
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PostMapping("/sync_value_deploy")
    public ResponseEntity<Void> syncValueToDeploy(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "更新信息", required = true)
            @RequestBody @Valid AppServiceSyncValueDeployVO syncValueDeployVO) {
        appServiceInstanceService.syncValueToDeploy(projectId, syncValueDeployVO);
        return Results.success();
    }

    @ApiOperation(value = "根据valueId查询需要同步实例的列表")
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @GetMapping("/list_instance_by_value_id")
    public ResponseEntity<List<AppServiceInstanceVO>> listInstanceByValueId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置ID", required = true)
            @Encrypt
            @RequestParam(value = "value_id") Long valueId,
            @ApiParam(value = "筛选参数", required = false)
            @RequestParam(value = "params", required = false) String params) {
        return Results.success(appServiceInstanceService.listInstanceByValueId(projectId, valueId, params));
    }
}
