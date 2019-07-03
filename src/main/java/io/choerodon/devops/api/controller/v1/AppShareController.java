package io.choerodon.devops.api.controller.v1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.AppMarketDownloadDTO;
import io.choerodon.devops.api.dto.AppMarketTgzDTO;
import io.choerodon.devops.api.dto.AppMarketVersionDTO;
import io.choerodon.devops.api.dto.AppVersionAndValueDTO;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.devops.app.service.AppShareService;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/apps_market")
public class AppShareController {
    private AppShareService appShareService;

    public AppShareController(AppShareService appShareService) {
        this.appShareService = appShareService;
    }

    /**
     * 应用发布
     *
     * @param projectId             项目id
     * @param applicationReleaseDTO 发布应用的信息
     * @return Long
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "应用发布")
    @PostMapping
    public ResponseEntity<Long> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "发布应用的信息", required = true)
            @RequestBody ApplicationReleasingDTO applicationReleaseDTO) {
        return Optional.ofNullable(
                appShareService.release(projectId, applicationReleaseDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.release"));
    }

    /**
     * 项目下查询所有发布在应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 搜索参数
     * @return list of ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下分页查询所有发布在应用市场的应用")
    @CustomPageRequest
    @PostMapping(value = "/list")
    public ResponseEntity<PageInfo<ApplicationReleasingDTO>> pageListMarketAppsByProjectId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                appShareService.listMarketAppsByProjectId(projectId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.applications.get"));
    }

    /**
     * 查询发布级别为全局或者在本组织下的所有应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 搜索参数
     * @return list of ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询发布级别为全局或者在本组织下的所有应用市场的应用")
    @CustomPageRequest
    @PostMapping(value = "/list_all")
    public ResponseEntity<PageInfo<ApplicationReleasingDTO>> listAllApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(appShareService.listMarketApps(projectId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.applications.query"));
    }

    /**
     * 查询项目下单个应用市场的应用详情
     *
     * @param projectId   项目id
     * @param appMarketId 发布ID
     * @return ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "查询项目下单个应用市场的应用详情")
    @GetMapping("/{app_market_id}/detail")
    public ResponseEntity<ApplicationReleasingDTO> queryAppInProject(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "发布ID", required = true)
            @PathVariable(value = "app_market_id") Long appMarketId) {
        return Optional.ofNullable(appShareService.getMarketAppInProject(projectId, appMarketId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.application.get"));
    }

    /**
     * 查询单个应用市场的应用
     *
     * @param projectId   项目id
     * @param appMarketId 发布ID
     * @return ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询单个应用市场的应用")
    @GetMapping("/{app_market_id}")
    public ResponseEntity<ApplicationReleasingDTO> queryApp(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "发布ID", required = true)
            @PathVariable(value = "app_market_id") Long appMarketId) {
        return Optional.ofNullable(appShareService.getMarketApp(appMarketId, null))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.application.get"));
    }


    /**
     * 查询单个应用市场的应用的版本
     *
     * @param projectId   项目id
     * @param appMarketId 发布ID
     * @return List of AppMarketVersionDTO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询项目下单个应用市场的应用的版本")
    @GetMapping("/{app_market_id}/versions")
    public ResponseEntity<List<AppMarketVersionDTO>> queryAppVersionsInProject(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "发布ID", required = true)
            @PathVariable(value = "app_market_id") Long appMarketId,
            @ApiParam(value = "是否发布", required = false)
            @RequestParam(value = "is_publish", required = false) Boolean isPublish) {
        return Optional.ofNullable(appShareService.getAppVersions(projectId, appMarketId, isPublish))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.application.versions.get"));
    }


    /**
     * 查询单个应用市场的应用的版本
     *
     * @param projectId   项目id
     * @param appMarketId 发布ID
     * @return Page of AppMarketVersionDTO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询项目下单个应用市场的应用的版本")
    @CustomPageRequest
    @PostMapping("/{app_market_id}/versions")
    public ResponseEntity<PageInfo<AppMarketVersionDTO>> queryAppVersionsInProjectByPage(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "发布ID", required = true)
            @PathVariable(value = "app_market_id") Long appMarketId,
            @ApiParam(value = "是否发布", required = false)
            @RequestParam(value = "is_publish", required = false) Boolean isPublish,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                appShareService.getAppVersions(projectId, appMarketId, isPublish, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.application.versions.query"));
    }

    /**
     * 查询单个应用市场的应用的单个版本README
     *
     * @param projectId   项目id
     * @param appMarketId 发布ID
     * @param versionId   版本ID
     * @return String of readme
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询单个应用市场的应用的单个版本README")
    @GetMapping("/{app_market_id}/versions/{version_id}/readme")
    public ResponseEntity<String> queryAppVersionReadme(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "发布ID", required = true)
            @PathVariable(value = "app_market_id") Long appMarketId,
            @ApiParam(value = "版本ID", required = true)
            @PathVariable(value = "version_id") Long versionId) {
        return Optional.ofNullable(
                appShareService.getMarketAppVersionReadme(appMarketId, versionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.application.readme.get"));
    }

    /**
     * 更新单个应用市场的应用
     *
     * @param projectId   项目id
     * @param appMarketId 发布ID
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "更新单个应用市场的应用")
    @PutMapping("/{app_market_id}")
    public ResponseEntity update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "发布ID", required = true)
            @PathVariable("app_market_id") Long appMarketId,
            @ApiParam(value = "发布应用的信息", required = true)
            @RequestBody(required = true) ApplicationReleasingDTO applicationRelease) {
        appShareService.update(projectId, appMarketId, applicationRelease);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 更新单个应用市场的应用版本
     *
     * @param projectId   项目id
     * @param appMarketId 发布ID
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "更新单个应用市场的应用")
    @PutMapping("/{app_market_id}/versions")
    public ResponseEntity updateVersions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "发布ID", required = true)
            @PathVariable("app_market_id") Long appMarketId,
            @ApiParam(value = "发布应用的信息", required = true)
            @RequestBody List<AppMarketVersionDTO> versionList) {
        appShareService.update(projectId, appMarketId, versionList);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 应用市场解析导入应用
     *
     * @param projectId 项目ID
     * @param file      文件
     * @return 应用列表
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "应用市场解析导入应用")
    @PostMapping("/upload")
    public ResponseEntity<AppMarketTgzDTO> uploadApps(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "文件", required = true)
            @RequestParam(value = "file") MultipartFile file) {
        return Optional.ofNullable(
                appShareService.getMarketAppListInFile(projectId, file))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.tgz.get"));
    }

    /**
     * 应用市场导入应用
     *
     * @param projectId 项目ID
     * @param fileName  文件名
     * @param isPublic  是否发布
     * @return 应用列表
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "应用市场导入应用")
    @PostMapping("/import")
    public ResponseEntity<Boolean> importApps(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "文件名", required = true)
            @RequestParam(value = "file_name") String fileName,
            @ApiParam(value = "是否公开")
            @RequestParam(value = "public", required = false) Boolean isPublic) {
        return Optional.ofNullable(
                appShareService.importApps(projectId, fileName, isPublic))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.market.import"));
    }

    /**
     * 应用市场取消导入应用
     *
     * @param projectId 项目ID
     * @param fileName  文件名
     * @return 应用列表
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "应用市场取消导入应用")
    @PostMapping("/import_cancel")
    public ResponseEntity deleteZip(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "文件名", required = true)
            @RequestParam(value = "file_name") String fileName) {
        appShareService.deleteZip(projectId, fileName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 导出应用市场应用信息
     *
     * @param projectId  项目id
     * @param appMarkets 应用市场应用信息
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "导出应用市场应用信息")
    @PostMapping("/export")
    public ResponseEntity exportFile(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "发布应用的信息", required = true)
            @RequestBody List<AppMarketDownloadDTO> appMarkets,
            @ApiParam(value = "导出包名字")
            @RequestParam(value = "fileName", required = false) String fileName,
            HttpServletResponse res) {
        appShareService.export(appMarkets, fileName);
        FileUtil.downloadFile(res, fileName + ".zip");
        try {
            Files.delete(new File(fileName + ".zip").toPath());
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询远程应用")
    @CustomPageRequest
    @PostMapping(value = "/remote_list")
    public ResponseEntity<PageInfo<ApplicationReleasingDTO>> pageListRemoteApps(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                appShareService.pageListRemoteApps(projectId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.remote.applications.get"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询远程应用版本")
    @CustomPageRequest
    @PostMapping(value = "/remote_list/versions")
    public ResponseEntity<PageInfo<ApplicationVersionRepDTO>> listVersionByAppId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = true)
            @PathVariable(value = "app_id") Long appId,
            @ApiParam(value = "access_token", required = true)
            @RequestParam(name = "access_token") String accessToken,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                appShareService.listVersionByAppId(appId, accessToken, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.remote.application.versions.get"));
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询远程应用版本详情")
    @CustomPageRequest
    @PostMapping(value = "/remote/config")
    public ResponseEntity<AppVersionAndValueDTO> getConfigInfoByVerionId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id", required = true)
            @RequestParam(value = "app_id") Long appId,
            @ApiParam(value = "版本Id", required = true)
            @RequestParam(name = "version_id") Long versionId,
            @ApiParam(value = "access_token", required = true)
            @RequestParam(name = "access_token") String accessToken) {
        return Optional.ofNullable(
                appShareService.getConfigInfoByVerionId(appId, versionId, accessToken))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.remote.version.config.get"));
    }
}
