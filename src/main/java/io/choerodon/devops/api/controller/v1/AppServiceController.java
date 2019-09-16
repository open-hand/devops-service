package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppServicePayload;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.enums.GitPlatformType;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by younger on 2018/4/4.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_service")
public class AppServiceController {

    private static final String ERROR_APPLICATION_GET = "error.app.service.get";
    private AppServiceService applicationServiceService;

    public AppServiceController(AppServiceService applicationServiceService) {
        this.applicationServiceService = applicationServiceService;
    }

    /**
     * 项目下创建服务
     *
     * @param projectId       项目id
     * @param appServiceReqVO 服务信息
     * @return ApplicationServiceRepVO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建服务")
    @PostMapping
    public ResponseEntity<AppServiceRepVO> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务信息", required = true)
            @RequestBody AppServiceReqVO appServiceReqVO) {
        return Optional.ofNullable(applicationServiceService.create(projectId, appServiceReqVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.create"));
    }

    /**
     * 项目下从外部代码库导入服务
     *
     * @param projectId          项目id
     * @param appServiceImportVO 服务信息
     * @return ApplicationServiceImportVO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下从外部代码库导入服务")
    @PostMapping("/import/external")
    public ResponseEntity<AppServiceRepVO> importApp(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务信息", required = true)
            @RequestBody AppServiceImportVO appServiceImportVO) {
        return Optional.ofNullable(applicationServiceService.importApp(projectId, appServiceImportVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.create"));
    }

    /**
     * 项目下查询单个服务信息
     *
     * @param projectId    项目id
     * @param appServiceId 服务Id
     * @return ApplicationRepDTO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询单个服务信息")
    @GetMapping("/{app_service_id}")
    public ResponseEntity<AppServiceRepVO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(applicationServiceService.query(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.query"));
    }

    /**
     * 项目下更新服务信息
     *
     * @param projectId           项目id
     * @param appServiceUpdateDTO 服务
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新服务信息")
    @PutMapping
    public ResponseEntity<Boolean> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务信息", required = true)
            @RequestBody AppServiceUpdateDTO appServiceUpdateDTO) {
        return Optional.ofNullable(applicationServiceService.update(projectId, appServiceUpdateDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.update"));
    }

    /**
     * 项目下启用停用服务信息
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param active       启用停用
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下启用停用服务信息")
    @PutMapping("/{app_service_id}")
    public ResponseEntity<Boolean> updateActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "启用停用", required = true)
            @RequestParam Boolean active) {
        return Optional.ofNullable(applicationServiceService.updateActive(projectId, appServiceId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.active"));
    }

    /**
     * 项目下删除创建失败服务
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下删除创建失败服务")
    @DeleteMapping("/{app_service_id}")
    public ResponseEntity delete(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        applicationServiceService.delete(projectId, appServiceId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下分页查询服务
     *
     * @param projectId   项目id
     * @param isActive    项目是否启用
     * @param appMarket   服务市场导入
     * @param pageRequest 分页参数
     * @param params      参数
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下分页查询服务")
    @CustomPageRequest
    @PostMapping("/page_by_options")
    public ResponseEntity<PageInfo<AppServiceRepVO>> pageByOptions(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务是否启用")
            @RequestParam(value = "active", required = false) Boolean isActive,
            @ApiParam(value = "服务是否存在版本")
            @RequestParam(value = "has_version", required = false) Boolean hasVersion,
            @ApiParam(value = "服务是否市场导入")
            @RequestParam(value = "app_market", required = false) Boolean appMarket,
            @ApiParam(value = "服务类型")
            @RequestParam(value = "type", required = false) String type,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.pageByOptions(projectId, isActive, hasVersion, appMarket, type, doPage, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.baseList"));
    }

    /**
     * 根据环境id分页获取已部署正在运行实例的服务
     *
     * @param projectId 项目id
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据环境id分页获取已部署正在运行实例的服务")
    @CustomPageRequest
    @GetMapping("/page_by_ids")
    public ResponseEntity<PageInfo<AppServiceCodeVO>> pageByEnvIdAndappServiceId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "服务 Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest) {
        return Optional.ofNullable(applicationServiceService.pageByIds(projectId, envId, appServiceId, pageRequest))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.query.ids"));
    }


    /**
     * 根据环境id获取已部署正在运行实例的服务
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param status    实例状态
     * @return List
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据环境id获取已部署正在运行实例的服务")
    @GetMapping("/list_by_env")
    public ResponseEntity<List<AppServiceCodeVO>> listByEnvIdAndStatus(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "实例运行状态")
            @RequestParam(required = false) String status,
            @ApiParam(value = "服务 Id")
            @RequestParam(required = false) Long appServiceId) {
        return Optional.ofNullable(applicationServiceService.listByEnvId(projectId, envId, status, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.query.env"));
    }

    /**
     * 项目下查询所有已经启用的服务
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询所有已经启用的服务")
    @GetMapping("/list_by_active")
    public ResponseEntity<List<AppServiceRepVO>> listByActive(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(applicationServiceService.listByActive(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APPLICATION_GET));
    }

    /**
     * 本项目下或者服务市场在该项目下部署过的服务
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "本项目下或者服务市场在该项目下部署过的服务")
    @GetMapping(value = "/list_all")
    public ResponseEntity<List<AppServiceRepVO>> listAll(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(applicationServiceService.listAll(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.baseList.all"));
    }

    /**
     * 创建服务校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      服务name
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建服务校验名称是否存在")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境名", required = true)
            @RequestParam String name) {
        applicationServiceService.checkNameByProjectId(projectId, name);
    }

    /**
     * 创建服务校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      服务code
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建服务校验编码是否存在")
    @GetMapping(value = "/check_code")
    public void checkCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务编码", required = true)
            @RequestParam String code) {
        applicationServiceService.checkCodeByProjectId(projectId, code);
    }

    /**
     * 批量校验appServiceCode和appServiceName
     *
     * @param projectId              项目ID
     * @param appServiceBatchCheckVO 服务code
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "批量校验appServiceCode和appServiceName")
    @PostMapping(value = "/batch_check")
    public ResponseEntity<AppServiceBatchCheckVO> batchCheck(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "校验数据", required = true)
            @RequestBody AppServiceBatchCheckVO appServiceBatchCheckVO) {
        return Optional.ofNullable(applicationServiceService.checkCodeByProjectId(projectId, appServiceBatchCheckVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.check"));
    }

    /**
     * 根据服务编码查询服务
     *
     * @param projectId 项目ID
     * @param code      服务code
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据服务编码查询服务")
    @GetMapping(value = "/query_by_code")
    public ResponseEntity<AppServiceRepVO> queryByCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务编码", required = true)
            @RequestParam String code) {
        return Optional.ofNullable(applicationServiceService.queryByCode(projectId, code))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APPLICATION_GET));
    }


    /**
     * 项目下查询已经启用有版本未发布的服务
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询所有已经启用的且未发布的且有版本的服务")
    @CustomPageRequest
    @PostMapping(value = "/page_unPublish")
    public ResponseEntity<PageInfo<AppServiceReqVO>> pageByActiveAndPubAndVersion(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(applicationServiceService.pageByActiveAndPubAndVersion(projectId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APPLICATION_GET));
    }

    /**
     * 项目下分页查询代码仓库
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      参数
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下分页查询代码仓库")
    @CustomPageRequest
    @PostMapping("/page_code_repository")
    public ResponseEntity<PageInfo<AppServiceRepVO>> pageCodeRepository(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.pageCodeRepository(projectId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APPLICATION_GET));
    }

    /**
     * 获取服务下所有用户权限
     *
     * @param appServiceId 服务id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "获取服务下所有用户权限")
    @GetMapping(value = "/{appServiceId}/list_all")
    public ResponseEntity<List<AppServiceUserPermissionRespVO>> listAllUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable Long appServiceId) {
        return Optional.ofNullable(applicationServiceService.listAllUserPermission(appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.user.permission.get"));
    }


    /**
     * 校验harbor配置信息是否正确
     *
     * @param url      harbor地址
     * @param userName harbor用户名
     * @param password harbor密码
     * @param project  harbor项目
     * @param email    harbor邮箱
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验harbor配置信息是否正确")
    @GetMapping(value = "/check_harbor")
    public void checkHarbor(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "harbor地址", required = true)
            @RequestParam String url,
            @ApiParam(value = "harbor用户名", required = true)
            @RequestParam String userName,
            @ApiParam(value = "harbor密码", required = true)
            @RequestParam String password,
            @ApiParam(value = "harborProject")
            @RequestParam(required = false) String project,
            @ApiParam(value = "harbor邮箱", required = true)
            @RequestParam String email) {
        applicationServiceService.checkHarbor(url, userName, password, project, email);
    }


    /**
     * 校验chart配置信息是否正确
     *
     * @param url chartmusume地址
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验chart配置信息是否正确")
    @GetMapping(value = "/check_chart")
    public void checkChart(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "chartmusume地址", required = true)
            @RequestParam String url) {
        applicationServiceService.checkChart(url);
    }

    /**
     * 验证用于克隆仓库的url及授权的access token是否有效
     *
     * @param platformType Git平台类型
     * @param url          用于克隆仓库的url(http/https)
     * @param accessToken  gitlab授权的access token
     * @return true 如果有效
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation("验证用于克隆仓库的url及授权的access token是否有效")
    @GetMapping("/url_validation")
    public ResponseEntity<Object> validateUrlAndAccessToken(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "Git平台类型", required = true)
            @RequestParam(value = "platform_type", required = false) String platformType,
            @ApiParam(value = "clone仓库的地址", required = true)
            @RequestParam(value = "url") String url,
            @ApiParam(value = "gitlab access token")
            @RequestParam(value = "access_token", required = false) String accessToken) {
        Boolean result = applicationServiceService.validateRepositoryUrlAndToken(GitPlatformType.from(platformType), url, accessToken);
        return new ResponseEntity<>(result == null ? "null" : result, HttpStatus.OK);
    }

    /**
     * 查看sonarqube相关信息
     *
     * @param projectId    项目Id
     * @param appServiceId 服务id
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("查看sonarqube相关信息")
    @GetMapping("/{app_service_id}/sonarqube")
    public ResponseEntity<SonarContentsVO> getSonarQube(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(applicationServiceService.getSonarContent(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.sonarqube.content.get"));
    }

    /**
     * 查看sonarqube相关报表
     *
     * @param projectId    项目Id
     * @param appServiceId 服务id
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("查看sonarqube相关信息")
    @GetMapping("/{app_service_id}/sonarqube_table")
    public ResponseEntity<SonarTableVO> getSonarQubeTable(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "类型", required = true)
            @RequestParam String type,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(applicationServiceService.getSonarTable(projectId, appServiceId, type, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.sonarqube.content.get"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询共享服务")
    @CustomPageRequest
    @PostMapping(value = "/page_share_app_service")
    public ResponseEntity<PageInfo<AppServiceRepVO>> pageShareApps(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                applicationServiceService.pageShareAppService(projectId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.app.service.get"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询拥有服务权限的项目成员及项目所有者")
    @CustomPageRequest
    @PostMapping(value = "/{app_service_id}/page_permission_users")
    public ResponseEntity<PageInfo<DevopsUserPermissionVO>> pagePermissionUsers(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务服务Id")
            @PathVariable(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                applicationServiceService.pagePermissionUsers(projectId, appServiceId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.app.service.user.permission.get"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询没有服务权限的项目成员")
    @GetMapping(value = "/{app_service_id}/list_non_permission_users")
    public ResponseEntity<List<DevopsUserPermissionVO>> listNonPermissionUsers(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务服务Id")
            @PathVariable(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "查询参数")
            @RequestParam(value = "param", required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.listMembers(projectId, appServiceId, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.app.service.no.user.permission.get"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "服务权限更新")
    @PostMapping(value = "/{app_service_id}/update_permission")
    public ResponseEntity updatePermission(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务服务Id")
            @PathVariable(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "权限信息", required = true)
            @RequestBody AppServicePermissionVO appServicePermissionVO) {
        applicationServiceService.updatePermission(projectId, appServiceId, appServicePermissionVO);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "服务权限删除")
    @DeleteMapping(value = "/{app_service_id}/delete_permission")
    public ResponseEntity deletePermission(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务服务Id")
            @PathVariable(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "user Id", required = true)
            @RequestParam(value = "user_id") Long userId) {
        applicationServiceService.deletePermission(projectId, appServiceId, userId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询组织下所有项目，除当前项目")
    @GetMapping(value = "/{organization_id}/list_projects")
    public ResponseEntity<List<ProjectVO>> listProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用服务Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "查询参数", required = true)
            @RequestParam(value = "params", required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.listProjects(organizationId, projectId, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.projects"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "导入应用")
    @PostMapping(value = "/import/internal")
    public ResponseEntity importAppService(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用信息", required = true)
            @RequestBody List<ApplicationImportInternalVO> importInternalVOS) {
        applicationServiceService.importAppServiceInternal(projectId, importInternalVOS);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分组查询应用服务")
    @GetMapping(value = "/list_app_group")
    public ResponseEntity<List<AppServiceGroupInfoVO>> listAppServiceGroup(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "市场来源", required = false)
            @RequestParam(required = false) Boolean share,
            @ApiParam(value = "查询条件", required = false)
            @RequestParam(required = false) String param) {
        return Optional.ofNullable(
                applicationServiceService.listAppServiceGroup(projectId, share, param))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.group.error"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询单个项目下的应用服务")
    @GetMapping(value = "/list_by_project_id")
    public ResponseEntity<List<AppServiceDTO>> listAppByProjectId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(
                applicationServiceService.listAppByProjectId(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.projectId.query"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询所有应用服务(应用服务导入、应用部署)")
    @GetMapping(value = "/list_all_app_services")
    public ResponseEntity<List<AppServiceGroupVO>> listAllAppServices(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "类型", required = true)
            @RequestParam(value = "type") String type,
            @ApiParam(value = "查询参数", required = false)
            @RequestParam(value = "param", required = false) String param,
            @ApiParam(value = "是否仅部署", required = true)
            @RequestParam(value = "deploy_only", required = true) Boolean deployOnly,
            @ApiParam(value = "应用服务类型", required = false)
            @RequestParam(value = "service_type", required = false) String serviceType) {
        return Optional.ofNullable(
                applicationServiceService.listAllAppServices(projectId, type, param, deployOnly, serviceType))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.service.deploy"));
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询所有应用服务(应用服务导入、应用部署)")
    @PostMapping(value = "/test")
    public void test(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用服务类型", required = false)
            @RequestBody DevOpsAppServicePayload devOpsAppServicePayload) {
        applicationServiceService.operationApplication(devOpsAppServicePayload);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "批量查询应用服务")
    @GetMapping(value = "/list_app_service_ids")
    public ResponseEntity<PageInfo<AppServiceVO>> batchQueryAppService(
            @ApiParam(value = "项目Id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用服务Ids")
            @RequestParam(value = "ids") Set<Long> ids,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.listAppServiceByIds(ids, doPage, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.service.ids"));
    }
}

