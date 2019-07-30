package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.ApplicationSevriceService;
import io.choerodon.devops.infra.enums.GitPlatformType;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * Created by younger on 2018/4/4.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/apps_service")
public class ApplicationServiceController {

    private static final String ERROR_APPLICATION_GET = "error.application.get";
    private ApplicationSevriceService applicationService;

    public ApplicationServiceController(ApplicationSevriceService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * 项目下创建应用
     *
     * @param projectId         项目id
     * @param applicationReqDTO 应用信息
     * @return ApplicationRepDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建应用")
    @PostMapping
    public ResponseEntity<ApplicationServiceRepVO> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用信息", required = true)
            @RequestBody ApplicationServiceReqVO applicationReqDTO) {
        return Optional.ofNullable(applicationService.create(projectId, applicationReqDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.create"));
    }

    /**
     * 项目下从外部代码库导入应用
     *
     * @param projectId           项目id
     * @param applicationImportVO 应用信息
     * @return ApplicationRepDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下从外部代码库导入应用")
    @PostMapping("/import")
    public ResponseEntity<ApplicationServiceRepVO> importApp(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用信息", required = true)
            @RequestBody ApplicationImportVO applicationImportVO) {
        return Optional.ofNullable(applicationService.importApp(projectId, applicationImportVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.create"));
    }

    /**
     * 项目下查询单个应用信息
     *
     * @param projectId     项目id
     * @param applicationId 应用Id
     * @return ApplicationRepDTO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询单个应用信息")
    @GetMapping("/{application_id}")
    public ResponseEntity<ApplicationServiceRepVO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId) {
        return Optional.ofNullable(applicationService.query(projectId, applicationId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.query"));
    }

    /**
     * 项目下更新应用信息
     *
     * @param projectId            项目id
     * @param applicationUpdateDTO 应用Id
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新应用信息")
    @PutMapping
    public ResponseEntity<Boolean> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用信息", required = true)
            @RequestBody ApplicationUpdateVO applicationUpdateDTO) {
        return Optional.ofNullable(applicationService.update(projectId, applicationUpdateDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.update"));
    }

    /**
     * 项目下启用停用应用信息
     *
     * @param projectId     项目id
     * @param applicationId 应用id
     * @param active        启用停用
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下启用停用应用信息")
    @PutMapping("/{application_id}")
    public ResponseEntity<Boolean> updateActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId,
            @ApiParam(value = "启用停用", required = true)
            @RequestParam Boolean active) {
        return Optional.ofNullable(applicationService.updateActive(applicationId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.active"));
    }

    /**
     * 项目下删除创建失败应用
     *
     * @param projectId     项目id
     * @param applicationId 应用id
     * @return Boolean
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下删除创建失败应用")
    @DeleteMapping("/{application_id}")
    public ResponseEntity delete(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "application_id") Long applicationId) {
        applicationService.delete(projectId, applicationId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下分页查询应用
     *
     * @param projectId   项目id
     * @param isActive    项目是否启用
     * @param appMarket   应用市场导入
     * @param pageRequest 分页参数
     * @param params      参数
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下分页查询应用")
    @CustomPageRequest
    @PostMapping("/page_by_options")
    public ResponseEntity<PageInfo<ApplicationServiceRepVO>> pageByOptions(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用是否启用")
            @RequestParam(value = "active", required = false) Boolean isActive,
            @ApiParam(value = "应用是否存在版本")
            @RequestParam(value = "has_version", required = false) Boolean hasVersion,
            @ApiParam(value = "应用是否存在版本")
            @RequestParam(value = "app_market", required = false) Boolean appMarket,
            @ApiParam(value = "应用类型")
            @RequestParam(value = "type", required = false) String type,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationService.pageByOptions(projectId, isActive, hasVersion, appMarket, type, doPage, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.baseList"));
    }

    /**
     * 根据环境id分页获取已部署正在运行实例的应用
     *
     * @param projectId 项目id
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据环境id分页获取已部署正在运行实例的应用")
    @CustomPageRequest
    @GetMapping("/page_by_ids")
    public ResponseEntity<PageInfo<ApplicationServiceCodeVO>> pageByEnvIdAndappServiceId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "应用 Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest) {
        return Optional.ofNullable(applicationService.pageByIds(projectId, envId, appServiceId, pageRequest))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.query.ids"));
    }


    /**
     * @param projectId
     * @param isActive
     * @param hasVersion
     * @param doPage
     * @param pageRequest
     * @param params
     * @return
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下分页查询应用 应用发布使用")
    @CustomPageRequest
    @PostMapping("/page_by_options/app_market")
    public ResponseEntity<PageInfo<ApplicationServiceRepVO>> pageByOptionsMarket(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用是否启用")
            @RequestParam(value = "active", required = false) Boolean isActive,
            @ApiParam(value = "应用是否存在版本")
            @RequestParam(value = "has_version", required = false) Boolean hasVersion,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationService.pageByOptionsAppMarket(projectId, isActive, hasVersion, null, null, doPage, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.appTemplate.get"));
    }

    /**
     * 根据环境id获取已部署正在运行实例的应用
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @param status    实例状态
     * @return List
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据环境id获取已部署正在运行实例的应用")
    @GetMapping("/list_by_env")
    public ResponseEntity<List<ApplicationServiceCodeVO>> listByEnvIdAndStatus(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "实例运行状态")
            @RequestParam(required = false) String status,
            @ApiParam(value = "应用 Id")
            @RequestParam(required = false) Long appServiceId) {
        return Optional.ofNullable(applicationService.listByEnvId(projectId, envId, status, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.query.env"));
    }

    /**
     * 项目下查询所有已经启用的应用
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询所有已经启用的应用")
    @GetMapping("/list_by_active")
    public ResponseEntity<List<ApplicationServiceRepVO>> listByActive(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(applicationService.listByActive(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APPLICATION_GET));
    }

    /**
     * 本项目下或者应用市场在该项目下部署过的应用
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "本项目下或者应用市场在该项目下部署过的应用")
    @GetMapping(value = "/list_all")
    public ResponseEntity<List<ApplicationServiceRepVO>> listAll(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(applicationService.listAll(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.applications.baseList.all"));
    }

    /**
     * 创建应用校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      应用name
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建应用校验名称是否存在")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境名", required = true)
            @RequestParam String name) {
        applicationService.checkName(projectId, name);
    }

    /**
     * 创建应用校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      应用code
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建应用校验编码是否存在")
    @GetMapping(value = "/check_code")
    public void checkCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用编码", required = true)
            @RequestParam String code) {
        applicationService.checkCode(projectId, code);
    }

    /**
     * 根据应用编码查询应用
     *
     * @param projectId 项目ID
     * @param code      应用code
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据应用编码查询应用")
    @GetMapping(value = "/query_by_code")
    public ResponseEntity<ApplicationServiceRepVO> queryByCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用编码", required = true)
            @RequestParam String code) {
        return Optional.ofNullable(applicationService.queryByCode(projectId, code))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APPLICATION_GET));
    }

    /**
     * 查询应用模板
     *
     * @param projectId 项目ID
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询所有应用模板")
    @GetMapping("/template")
    public ResponseEntity<List<ApplicationTemplateRespVO>> listTemplate(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否只查询预定义")
            @RequestParam(required = false) Boolean isPredefined) {
        return Optional.ofNullable(applicationService.listTemplate(projectId, isPredefined))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.template.get"));
    }

    /**
     * 项目下查询已经启用有版本未发布的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return Page
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询所有已经启用的且未发布的且有版本的应用")
    @CustomPageRequest
    @PostMapping(value = "/page_unPublish")
    public ResponseEntity<PageInfo<ApplicationServiceReqVO>> pageByActiveAndPubAndVersion(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(applicationService.pageByActiveAndPubAndVersion(projectId, pageRequest, params))
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
    public ResponseEntity<PageInfo<ApplicationServiceRepVO>> pageCodeRepository(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationService.pageCodeRepository(projectId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APPLICATION_GET));
    }

    /**
     * 获取应用下所有用户权限
     *
     * @param appServiceId 应用id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "获取应用下所有用户权限")
    @GetMapping(value = "/{appServiceId}/list_all")
    public ResponseEntity<List<AppServiceUserPermissionRespVO>> listAllUserPermission(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long appServiceId) {
        return Optional.ofNullable(applicationService.listAllUserPermission(appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.user.permission.get"));
    }


    /**
     * 校验harbor配置信息是否正确
     *
     * @param url      harbor地址
     * @param userName harbor用户名
     * @param passWord harbor密码
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
            @RequestParam String passWord,
            @ApiParam(value = "harborProject", required = true)
            @RequestParam String project,
            @ApiParam(value = "harbor邮箱", required = true)
            @RequestParam String email) {
        applicationService.checkHarbor(url, userName, passWord, project, email);
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
        applicationService.checkChart(url);
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
        Boolean result = applicationService.validateRepositoryUrlAndToken(GitPlatformType.from(platformType), url, accessToken);
        return new ResponseEntity<>(result == null ? "null" : result, HttpStatus.OK);
    }

    /**
     * 查看sonarqube相关信息
     *
     * @param projectId 项目Id
     * @param appServiceId     应用id
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("查看sonarqube相关信息")
    @GetMapping("/{app_service_id}/sonarqube")
    public ResponseEntity<SonarContentsVO> getSonarQube(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(applicationService.getSonarContent(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.sonarqube.content.get"));
    }

    /**
     * 查看sonarqube相关报表
     *
     * @param projectId 项目Id
     * @param appServiceId     应用id
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("查看sonarqube相关信息")
    @GetMapping("/{app_service_id}/sonarqube_table")
    public ResponseEntity<SonarTableVO> getSonarQubeTable(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "类型", required = true)
            @RequestParam String type,
            @ApiParam(value = "startTime")
            @RequestParam(required = true) Date startTime,
            @ApiParam(value = "endTime")
            @RequestParam(required = true) Date endTime) {
        return Optional.ofNullable(applicationService.getSonarTable(projectId, appServiceId, type, startTime, endTime))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.sonarqube.content.get"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询远程应用")
    @CustomPageRequest
    @GetMapping(value = "/page_remote_apps")
    public ResponseEntity<PageInfo<RemoteApplicationServiceVO>> pageRemoteApps(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestParam(value = "params", required = false) String params) {
        return Optional.ofNullable(
                applicationService.pageRemoteApps(projectId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.remote.applications.get"));
    }


    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询共享应用")
    @CustomPageRequest
    @PostMapping(value = "/page_share_apps")
    public ResponseEntity<PageInfo<ApplicationServiceRepVO>> pageShareApps(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                applicationService.pageShareApps(projectId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.shere.applications.get"));
    }
}
