package io.choerodon.devops.api.controller.v1;

import java.util.*;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.enums.GitPlatformType;
import io.choerodon.devops.infra.enums.deploy.ApplicationCenterEnum;
import io.choerodon.mybatis.pagehelper.annotation.PageableDefault;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by younger on 2018/4/4.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_service")
public class AppServiceController {

    private static final String ERROR_APPLICATION_GET = "error.app.service.get";
    private final AppServiceService applicationServiceService;

    public AppServiceController(AppServiceService applicationServiceService) {
        this.applicationServiceService = applicationServiceService;
    }

    @ApiOperation("内部查询项目下的应用服务 / 不区分权限")
    @PostMapping("page_by_options_internal")
    @Permission(permissionWithin = true)
    @CustomPageRequest
    public ResponseEntity<Page<AppServiceRepVO>> internalListAll(
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @RequestBody(required = false) String params) {
        return Results.success(applicationServiceService.internalListAllInProject(projectId, params, pageable));
    }

    /**
     * 项目下创建应用服务
     *
     * @param projectId       项目id
     * @param appServiceReqVO 服务信息
     * @return ApplicationServiceRepVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建应用服务")
    @PostMapping
    public ResponseEntity<AppServiceRepVO> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务信息", required = true)
            @RequestBody @Validated AppServiceReqVO appServiceReqVO) {
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
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下从外部代码库导入服务")
    @PostMapping("/import/external")
    public ResponseEntity<AppServiceRepVO> importApp(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否系统模板", required = false)
            @RequestParam(value = "is_template", required = false) Boolean isTemplate,
            @ApiParam(value = "服务信息", required = true)
            @RequestBody AppServiceImportVO appServiceImportVO) {
        return Optional.ofNullable(applicationServiceService.importApp(projectId, appServiceImportVO, isTemplate))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.create"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下查询用户在该组织下拥有权限的应用")
    @CustomPageRequest
    @GetMapping("/list_service_under_org")
    public ResponseEntity<Page<AppServiceUnderOrgVO>> appServiceUnderOrg(
            @ApiParam(value = "项目Id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id")
            @Encrypt @RequestParam(name = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "搜索参数")
            @RequestParam(name = "param", required = false) String searchParam,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest
    ) {
        return ResponseEntity.ok(applicationServiceService.listAppServiceUnderOrg(projectId, appServiceId, searchParam, pageRequest));
    }

    /**
     * 项目下查询单个服务信息
     *
     * @param projectId    项目id
     * @param appServiceId 服务Id
     * @return ApplicationRepDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询单个服务信息")
    @GetMapping("/{app_service_id}")
    public ResponseEntity<AppServiceRepVO> query(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(applicationServiceService.query(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.query"));
    }

    /**
     * 项目下查询单个应用服务信息(操作其他项目应用时使用)
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下查询单个应用服务信息(操作其他项目应用时使用)")
    @GetMapping("/other/{app_service_id}")
    public ResponseEntity<AppServiceRepVO> queryOtherProjectAppService(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(applicationServiceService.queryOtherProjectAppServiceWithRepositoryInfo(projectId, appServiceId))
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
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
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
     * 项目下启用停用应用服务
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @param active       启用停用
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下启用停用应用服务")
    @PutMapping("/{app_service_id}")
    public ResponseEntity<Boolean> updateActive(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务id", required = true)
            @Encrypt
            @PathVariable(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "启用停用", required = true)
            @RequestParam Boolean active) {
        return Optional.ofNullable(applicationServiceService.updateActive(projectId, appServiceId, active))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.active"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下校验是否能够停用服务")
    @GetMapping("/check/{app_service_id}")
    public ResponseEntity<AppServiceMsgVO> checkAppService(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(applicationServiceService.checkAppService(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.check"));
    }

    /**
     * 项目下删除创建失败服务
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下删除创建应用服务")
    @DeleteMapping("/{app_service_id}")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        applicationServiceService.delete(projectId, appServiceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 项目下分页查询服务
     *
     * @param projectId 项目id
     * @param isActive  项目是否启用
     * @param appMarket 服务市场导入
     * @param pageable  分页参数
     * @param params    参数
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下分页查询应用服务")
    @CustomPageRequest
    @PostMapping("/page_by_options")
    public ResponseEntity<Page<AppServiceRepVO>> pageByOptions(
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
            @ApiParam(value = "是否校验团队成员权限")
            @RequestParam(value = "checkMember", required = false, defaultValue = "false") Boolean checkMember,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.pageByOptions(projectId, isActive, hasVersion, appMarket, type, doPage, pageable, params, checkMember))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.baseList"));
    }

    /**
     * 根据环境id分页获取已部署正在运行实例的服务
     *
     * @param projectId 项目id
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据环境id分页获取已部署正在运行实例的服务")
    @CustomPageRequest
    @GetMapping("/page_by_ids")
    public ResponseEntity<Page<AppServiceCodeVO>> pageByEnvIdAndappServiceId(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境 ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "服务 Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable) {
        return Optional.ofNullable(applicationServiceService.pageByIds(projectId, envId, appServiceId, pageable))
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
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据环境id获取已部署正在运行实例的服务")
    @GetMapping("/list_by_env")
    public ResponseEntity<List<AppServiceCodeVO>> listByEnvIdAndStatus(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境 ID", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "实例运行状态")
            @RequestParam(required = false) String status,
            @ApiParam(value = "服务 Id")
            @Encrypt
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
    @Permission(level = ResourceLevel.ORGANIZATION,
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
     * 项目下查询所有已经启用服务数量
     *
     * @param projectId 项目id
     * @return Integer
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询所有已经启用服务数量")
    @GetMapping("/count_by_active")
    public ResponseEntity<Integer> countByActive(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(applicationServiceService.countByActive(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APPLICATION_GET));
    }

    /**
     * 查询在此项目下生成了实例的服务
     *
     * @param projectId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询在此项目下生成了实例的服务")
    @GetMapping(value = "/list_all")
    public ResponseEntity<List<AppServiceRepVO>> listAll(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestParam(value = "appServiceName", required = false) String appServiceName) {
        return Optional.ofNullable(applicationServiceService.listAll(projectId, appServiceName))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.service.baseList.all"));
    }

    /**
     * 创建服务时校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      服务name
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建服务时校验名称是否存在")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境名", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(applicationServiceService.isNameUnique(projectId, name));
    }

    /**
     * 创建服务时校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      服务code
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建服务时校验编码是否存在")
    @GetMapping(value = "/check_code")
    public ResponseEntity<Boolean> checkCode(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务编码", required = true)
            @RequestParam String code) {
        return ResponseEntity.ok(applicationServiceService.isCodeUnique(projectId, code));
    }

    /**
     * 批量校验appServiceCode和appServiceName
     *
     * @param projectId              项目ID
     * @param appServiceBatchCheckVO 服务code
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
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
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
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


//    /**
//     * 项目下查询已经启用有版本未发布的服务
//     *
//     * @param projectId 项目id
//     * @param pageable  分页参数
//     * @param params    查询参数
//     * @return Page
//     */
//    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
//    @ApiOperation(value = "项目下查询所有已经启用的且未发布的且有版本的服务")
//    @CustomPageRequest
//    @PostMapping(value = "/page_unPublish")
//    public ResponseEntity<Page<AppServiceReqVO>> pageByActiveAndPubAndVersion(
//            @ApiParam(value = "项目 ID", required = true)
//            @PathVariable(value = "project_id") Long projectId,
//            @ApiParam(value = "分页参数")
//            @ApiIgnore PageRequest pageable,
//            @ApiParam(value = "查询参数")
//            @RequestBody(required = false) String params) {
//        return Optional.ofNullable(applicationServiceService.pageByActiveAndPubAndVersion(projectId, pageable, params))
//                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
//                .orElseThrow(() -> new CommonException(ERROR_APPLICATION_GET));
//    }

    /**
     * 校验chart仓库配置信息是否正确
     *
     * @param configVO chartMuseum信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验chart仓库配置信息是否正确")
    @PostMapping(value = "/check_chart")
    public void checkChart(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "chartMuseum信息", required = true)
            @RequestBody ConfigVO configVO) {
        applicationServiceService.checkChart(configVO.getUrl(), configVO.getUserName(), configVO.getPassword());
    }

    /**
     * 验证用于克隆仓库的url及授权的access token是否有效
     *
     * @param platformType Git平台类型
     * @param url          用于克隆仓库的url(http/https)
     * @param accessToken  gitlab授权的access token
     * @return true 如果有效
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
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
     * @return sonarqube相关信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("查看sonarqube相关信息")
    @GetMapping("/{app_service_id}/sonarqube")
    public ResponseEntity<SonarContentsVO> getSonarQube(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Results.success(applicationServiceService.getSonarContent(projectId, appServiceId));
    }

    /**
     * 查看sonarqube相关报表
     *
     * @param projectId    项目Id
     * @param appServiceId 服务id
     * @return sonarqube相关报表
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation("查看sonarqube相关信息")
    @GetMapping("/{app_service_id}/sonarqube_table")
    public ResponseEntity<SonarTableVO> getSonarQubeTable(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
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

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下分页查询共享服务")
    @CustomPageRequest
    @PostMapping(value = "/page_share_app_service")
    public ResponseEntity<Page<AppServiceRepVO>> pageShareApps(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false, defaultValue = "true") Boolean doPage,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                applicationServiceService.pageShareAppService(projectId, doPage, pageable, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.app.service.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询拥有应用服务权限的用户")
    @CustomPageRequest
    @PostMapping(value = "/{app_service_id}/page_permission_users")
    public ResponseEntity<Page<DevopsUserPermissionVO>> pagePermissionUsers(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务服务Id")
            @PathVariable(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                applicationServiceService.pagePermissionUsers(projectId, appServiceId, pageable, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.share.app.service.user.permission.get"));
    }


    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询组织下所有项目，除当前项目")
    @GetMapping(value = "/{organization_id}/list_projects")
    public ResponseEntity<List<ProjectVO>> listProjects(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "查询参数", required = true)
            @RequestParam(value = "params", required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.listProjects(organizationId, projectId, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.projects"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "从平台内部导入应用服务")
    @PostMapping(value = "/import/internal")
    public ResponseEntity<Void> importAppService(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用信息", required = true)
            @RequestBody List<ApplicationImportInternalVO> importInternalVOS) {
        applicationServiceService.importAppServiceInternal(projectId, importInternalVOS);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "导入应用下根据组织共享或者市场下载查询应用服务")
    @GetMapping(value = "/page_by_mode")
    public ResponseEntity<Page<AppServiceGroupInfoVO>> listAppServiceGroup(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "市场来源", required = true)
            @RequestParam(required = true) Boolean share,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询项目Id", required = false)
            @RequestParam(value = "search_project_id", required = false) Long searchProjectId,
            @ApiParam(value = "查询条件", required = false)
            @RequestParam(required = false) String param) {
        return Optional.ofNullable(
                applicationServiceService.pageAppServiceByMode(projectId, Boolean.TRUE, searchProjectId, param, pageable))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.group.error"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询单个项目下的应用服务")
    @PostMapping(value = "/list_by_project_id")
    public ResponseEntity<Page<AppServiceVO>> listAppByProjectId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.listAppByProjectId(projectId, doPage, pageable, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.projectId.query"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
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

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "批量查询应用服务")
    @PostMapping(value = "/list_app_service_ids")
    public ResponseEntity<Page<AppServiceVO>> batchQueryAppService(
            @ApiParam(value = "项目Id")
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务Ids")
            @RequestParam(value = "ids") Set<Long> ids,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "是否需要版本信息", required = false)
            @RequestParam(value = "with_version", required = false, defaultValue = "true") boolean withVersion,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.listAppServiceByIds(projectId, ids, doPage, withVersion, pageable, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.service.ids"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "通过一组id分页查询或者不传id时进行分页查询")
    @PostMapping(value = "/list_by_ids_or_page")
    public ResponseEntity<Page<AppServiceVO>> listOrPage(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false, defaultValue = "true") Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "应用服务Ids")
            @Encrypt
            @RequestBody(required = false) Set<Long> ids) {
        return Optional.ofNullable(
                applicationServiceService.listByIdsOrPage(projectId, ids, doPage, pageable))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.service.ids"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据导入类型查询应用服务所属的项目集合")
    @GetMapping(value = "/list_project_by_share")
    public ResponseEntity<List<ProjectVO>> listProjectByShare(
            @ApiParam(value = "项目Id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "导入应用服务类型")
            @RequestParam(value = "share") Boolean share) {
        return new ResponseEntity<>(applicationServiceService.listProjectByShare(projectId, share), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据多个版本Id查询多个应用服务")
    @GetMapping(value = "/list_service_by_version_ids")
    public ResponseEntity<List<AppServiceVO>> listServiceByVersionIds(
            @ApiParam(value = "项目Id")
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用服务Ids")
            @Encrypt
            @RequestParam(value = "version_ids") Set<Long> ids) {
        return Optional.ofNullable(
                applicationServiceService.listServiceByVersionIds(ids))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.service.by.version.ids"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询应用服务模板")
    @GetMapping(value = "/list_service_templates")
    public ResponseEntity<List<AppServiceTemplateVO>> listServiceTemplates(
            @ApiParam(value = "项目Id")
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(
                applicationServiceService.listServiceTemplates())
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.service.templates"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "列出项目下普通应用服务，任何角色可以查到所有的的应用服务")
    @GetMapping(value = "/list_app_services_having_versions")
    public ResponseEntity<List<AppServiceSimpleVO>> listHavingVersions(
            @ApiParam(value = "项目Id")
            @PathVariable(value = "project_id") Long projectId) {
        return new ResponseEntity<>(applicationServiceService.listAppServiceHavingVersions(projectId), HttpStatus.OK);
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "查询项目下应用服务的数量")
    @PostMapping("/list_by_project_ids")
    public ResponseEntity<Map<Long, Integer>> countByProjectIds(
            @ApiParam(value = "项目Id")
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody List<Long> projectIds) {
        return new ResponseEntity<>(applicationServiceService.countByProjectId(projectIds), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "检查是否还能创建应用服务")
    @GetMapping("/check_enable_create")
    public ResponseEntity<Boolean> checkEnableCreateAppSvc(@PathVariable(name = "project_id") Long projectId) {
        return ResponseEntity.ok(applicationServiceService.checkEnableCreateAppSvc(projectId));
    }

    /**
     * 查询用于创建CI流水线的应用服务
     * 1. 默认查询20条
     * 2. 要用户有权限的
     * 3. 要创建成功且启用的
     * 4. 要能够模糊搜索
     * 5. 不能查出已经有流水线的
     * 6. 要有master分支的
     *
     * @param projectId 项目id
     * @param params    查询参数，用于搜索
     * @return 应用服务列表
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询没有CI流水线的应用服务")
    @CustomPageRequest
    @PostMapping("/page_app_services_without_ci")
    public ResponseEntity<List<AppServiceSimpleVO>> pageAppServiceWithoutCiPipeline(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(name = "project_id") Long projectId,
            @ApiIgnore @PageableDefault() PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(applicationServiceService.pageAppServiceToCreateCiPipeline(projectId, pageRequest, params));
    }


    /**
     * 获取项目下应用服务的数量
     *
     * @return 环应用服务的数量
     */
    @ApiOperation("获取项目下应用服务的数量")
    @Permission(permissionWithin = true)
    @GetMapping("/count_by_options")
    public ResponseEntity<Long> countAppCountByOptions(
            @ApiParam("项目id")
            @PathVariable("project_id") Long projectId) {
        return new ResponseEntity<>(applicationServiceService.countAppCountByOptions(projectId), HttpStatus.OK);
    }

    /**
     * @param envId  为null代表查全部环境
     * @param type   ,项目下应用project,市场应用market 共享 share 全部 all
     * @param params
     * @return
     */
    @ApiOperation("应用中心")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/app_center")
    @CustomPageRequest
    public ResponseEntity<Page<AppServiceRepVO>> applicationCenter(
            @PathVariable("project_id") Long projectId,
            @Encrypt @RequestParam(value = "envId", required = false) Long envId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "params", required = false) String params,
            @ApiIgnore @PageableDefault() PageRequest pageRequest) {
        return ResponseEntity.ok(applicationServiceService.applicationCenter(projectId, envId, type, params, pageRequest));
    }

    @ApiOperation("查询应用服务所关联的环境列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/app_center/envs/by_app_id")
    public ResponseEntity<List<DevopsEnvironmentRepVO>> listEnvByAppServiceId(
            @PathVariable("project_id") Long projectId,
            @Encrypt @RequestParam(value = "appServiceId") Long appServiceId) {
        return ResponseEntity.ok(applicationServiceService.listEnvByAppServiceId(projectId, appServiceId));
    }

    @ApiOperation("检查是否可以删除关联")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/app_center/env_app")
    public ResponseEntity<Boolean> checkDeleteEnvApp(
            @Encrypt @RequestParam(value = "appServiceId") Long appServiceId,
            @Encrypt @RequestParam(value = "envId") Long envId) {
        return ResponseEntity.ok(applicationServiceService.checkDeleteEnvApp(appServiceId, envId));
    }
}


