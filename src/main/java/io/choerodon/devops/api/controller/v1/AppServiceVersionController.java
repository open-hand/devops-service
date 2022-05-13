package io.choerodon.devops.api.controller.v1;

import java.util.*;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppServiceVersionAndCommitVO;
import io.choerodon.devops.api.vo.AppServiceVersionRespVO;
import io.choerodon.devops.api.vo.AppServiceVersionVO;
import io.choerodon.devops.api.vo.AppServiceVersionWithHelmConfigVO;
import io.choerodon.devops.app.service.AppServiceVersionService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Zenger on 2018/4/3.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/app_service_versions")
public class AppServiceVersionController {

    private static final String VERSION_QUERY_ERROR = "error.application.version.query";

    @Autowired
    private AppServiceVersionService appServiceVersionService;


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询服务版本")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<Page<AppServiceVersionVO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务Id")
            @RequestParam(value = "app_service_id") Long appServiceId,
            @Encrypt
            @ApiParam(value = "服务版本Id")
            @RequestParam(value = "app_service_version_id", required = false) Long appServiceVersionId,
            @ApiParam(value = "是否仅部署")
            @RequestParam(value = "deploy_only") Boolean deployOnly,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "do_page", required = false) Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params,
            @ApiParam(value = "指定版本")
            @RequestParam(required = false) String version) {
        return Optional.ofNullable(appServiceVersionService.pageByOptions(
                projectId, appServiceId, appServiceVersionId, deployOnly, doPage, params, pageable, version))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }


    /**
     * 项目下查询服务所有已部署版本
     *
     * @param projectId    项目id
     * @param appServiceId 服务Id
     * @return List
     */
    @ApiOperation(value = "项目下查询服务所有已部署版本")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/list_deployed_by_app_service/{app_service_id}")
    public ResponseEntity<List<AppServiceVersionRespVO>> queryDeployedByAppServiceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务Id")
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(appServiceVersionService.listDeployedByAppId(projectId, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 查询部署在某个环境的服务版本
     *
     * @param projectId    项目id
     * @param appServiceId 服务Id
     * @param envId        环境Id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询部署在某个环境的应用服务的版本")
    @GetMapping("/app/{app_service_id}/env/{envId}/query")
    public ResponseEntity<List<AppServiceVersionRespVO>> queryByappServiceIdAndEnvId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务Id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            @Encrypt
            @ApiParam(value = "环境 ID", required = true)
            @PathVariable Long envId) {
        return Optional.ofNullable(appServiceVersionService.listByAppIdAndEnvId(projectId, appServiceId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 根据服务版本ID查询，可升级的服务版本
     *
     * @param projectId           项目ID
     * @param appServiceServiceId 服务版本ID
     * @return ApplicationVersionRespVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "实例下查询可升级版本")
    @GetMapping(value = "/version/{app_version_id}/upgrade_version")
    public ResponseEntity<List<AppServiceVersionRespVO>> listUpgradeableAppVersion(
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务版本ID", required = true)
            @PathVariable(value = "app_version_id") Long appServiceServiceId) {
        return Optional.ofNullable(appServiceVersionService.listUpgradeableAppVersion(projectId, appServiceServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }


    /**
     * 根据版本id获取版本values
     *
     * @param projectId 项目ID
     * @param versionId 服务版本ID
     * @return String
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据版本id获取版本values")
    @GetMapping(value = "/{versionId}/queryValue")
    public ResponseEntity<String> queryVersionValue(
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务版本ID", required = true)
            @PathVariable(value = "versionId") Long versionId) {
        return Optional.ofNullable(appServiceVersionService.queryVersionValue(versionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.value.query"));
    }


    /**
     * 根据版本id查询版本信息
     *
     * @param projectId  项目ID
     * @param versionIds 服务版本ID
     * @return ApplicationVersionRespVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据版本id查询版本信息(新用的地方用下面的接口，不要用这个)")
    @PostMapping(value = "/list_by_versionIds")
    public ResponseEntity<List<AppServiceVersionRespVO>> queryAppServiceVersionsByIds(
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务ID", required = true)
            @RequestParam Long[] versionIds) {
        return Optional.ofNullable(appServiceVersionService.listByAppServiceVersionIds(Arrays.asList(versionIds)))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 根据版本id查询版本信息
     *
     * @param projectId  项目ID
     * @param versionIds 服务版本ID
     * @return ApplicationVersionRespVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据版本id查询版本信息")
    @PostMapping(value = "/list_by_version_ids")
    public ResponseEntity<List<AppServiceVersionRespVO>> listAppServiceVersionsByIds(
            @Encrypt
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务id", required = true)
            @RequestBody Set<Long> versionIds) {
        return Optional.ofNullable(appServiceVersionService.listByAppServiceVersionIds(new ArrayList<>(versionIds)))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }


    /**
     * 根据分支名查询版本
     *
     * @param projectId    项目ID
     * @param branch       分支
     * @param appServiceId 服务Id
     * @return ApplicationVersionRespVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据分支名查询版本")
    @GetMapping(value = "/list_by_branch")
    public ResponseEntity<List<AppServiceVersionAndCommitVO>> listAppVersionsByBranch(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务ID", required = true)
            @RequestParam Long appServiceId,
            @ApiParam(value = "分支", required = true)
            @RequestParam String branch) {
        return Optional.ofNullable(appServiceVersionService.listByAppIdAndBranch(appServiceId, branch))
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
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据pipelineID 查询版本, 判断是否存在")
    @GetMapping(value = "/query_by_pipeline")
    public ResponseEntity<Boolean> queryByPipeline(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "持续集成Id", required = true)
            @RequestParam(value = "pipeline_id") Long pipelineId,
            @Encrypt
            @ApiParam(value = "服务Id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分支", required = true)
            @RequestParam String branch) {
        return Optional.ofNullable(appServiceVersionService.queryByPipelineId(pipelineId, branch, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }

    /**
     * 根据服务ID查询最新生成版本
     *
     * @param projectId    项目id
     * @param appServiceId 服务id
     * @return 最新版本
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据服务ID查询最新生成版本的values")
    @GetMapping("/value")
    public ResponseEntity<String> queryByProjectId(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务Id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId) {
        return ResponseEntity.ok(appServiceVersionService.queryValueById(projectId, appServiceId));
    }

    /**
     * 根据服务和版本号查询服务版本
     *
     * @param projectId    项目ID
     * @param appServiceId 服务Id
     * @param version      版本
     * @return ApplicationVersionRespVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据服务和版本号查询服务版本")
    @GetMapping(value = "/query_by_version")
    public ResponseEntity<AppServiceVersionRespVO> queryByVersion(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务Id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "版本号", required = true)
            @RequestParam String version) {
        return Optional.ofNullable(appServiceVersionService.queryByAppAndVersion(appServiceId, version))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(VERSION_QUERY_ERROR));
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下查询共享服务版本")
    @CustomPageRequest
    @PostMapping(value = "/page_share/versions")
    public ResponseEntity<Page<AppServiceVersionRespVO>> pageShareVersionByappServiceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务Id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestParam(value = "version", required = false) String version) {
        return Optional.ofNullable(
                appServiceVersionService.pageShareVersionByAppId(appServiceId, pageable, version))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.remote.application.versions.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下分页查询共享服务版本或者根据版本信息模糊查询")
    @CustomPageRequest
    @GetMapping(value = "/page/share_versions")
    public ResponseEntity<Page<AppServiceVersionRespVO>> pageShareVersionByAppServiceIdAndVersion(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务Id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
            @ApiParam(value = "版本信息")
            @RequestParam(value = "version", required = false) String version) {
        return Optional.ofNullable(
                        appServiceVersionService.pageShareVersionByAppServiceIdAndVersion(appServiceId, pageable, version))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.remote.application.versions.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目下查询共享服务的所有共享版本")
    @GetMapping(value = "/{app_service_id}/list_share_versions")
    public ResponseEntity<List<AppServiceVersionVO>> listAppServiceVersionByShareAndAppSerivceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "服务Id", required = true)
            @PathVariable(value = "app_service_id", required = true) Long appServiceId,
            @ApiParam(value = "共享形式")
            @RequestParam(required = false, value = "share") String share) {
        return Optional.ofNullable(
                appServiceVersionService.queryServiceVersionByAppServiceIdAndShare(appServiceId, share))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.remote.application.versions.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据应用服务Id集合查询所有应用版本")
    @GetMapping(value = "/list_by_service_ids")
    public ResponseEntity<List<AppServiceVersionVO>> listVersionByIds(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务id集合", required = true)
            @RequestParam(value = "app_service_ids", required = true) Set<Long> ids) {
        return Optional.ofNullable(
                appServiceVersionService.listServiceVersionVoByIds(ids))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.versions.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据应用服务Id查询所有应用版本")
    @GetMapping(value = "/list_by_service_id")
    public ResponseEntity<List<AppServiceVersionVO>> listVersionById(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务id", required = true)
            @RequestParam(value = "app_service_id", required = true) Long id,
            @ApiParam(value = "查询参数", required = false)
            @RequestParam(value = "params", required = false) String params) {
        return Optional.ofNullable(
                appServiceVersionService.listVersionById(projectId, id, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.versions.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "批量删除应用服务版本")
    @DeleteMapping(value = "/batch")
    public ResponseEntity<Void> batchDelete(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @Encrypt @RequestBody Set<Long> versionIds
    ) {
        appServiceVersionService.batchDelete(projectId, appServiceId, versionIds);
        return ResponseEntity.noContent().build();
    }


    @Permission(level = ResourceLevel.ORGANIZATION, permissionWithin = true)
    @ApiOperation(value = "根据id查询版本信息及helm配置/内部接口，market-service用")
    @GetMapping(value = "/version_with_helm_config")
    public ResponseEntity<AppServiceVersionWithHelmConfigVO> queryVersionWithHelmConfig(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务版本id", required = true)
            @RequestParam(value = "app_service_version_id") Long appServiceVersionId) {
        return ResponseEntity.ok(appServiceVersionService.queryVersionWithHelmConfig(projectId, appServiceVersionId));
    }
}
