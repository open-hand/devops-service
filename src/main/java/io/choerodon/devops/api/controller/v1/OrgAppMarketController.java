package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.HarborMarketVO;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.OrgAppMarketService;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:30 2019/6/28
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/organizations/app_market")
public class OrgAppMarketController {
    @Autowired
    private OrgAppMarketService orgAppMarketService;

    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "查询所有应用服务")
    @GetMapping("/list_app_services")
    public ResponseEntity<List<AppServiceUploadPayload>> listAllAppServices() {
        return Optional.ofNullable(
                orgAppMarketService.listAllAppServices())
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.services.listAll"));
    }

    /**
     * @param appId
     * @param pageRequest
     * @param params
     * @return
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "根据应用Id，获取应用服务和应用服务版本")
    @CustomPageRequest
    @PostMapping("/page_app_services")
    public ResponseEntity<PageInfo<AppServiceUploadPayload>> pageByAppId(
            @ApiParam(value = "应用Id", required = true)
            @RequestParam(value = "app_id") Long appId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数", required = false)
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                orgAppMarketService.pageByAppId(appId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.services.page"));
    }

    /**
     * @param appServiceId
     * @return
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "根据应用服务ID查询所对应的应用版本")
    @GetMapping("/list_versions/{app_service_id}")
    public ResponseEntity<List<AppServiceVersionUploadPayload>> listVersionsByAppServiceId(
            @ApiParam(value = "应用服务Id")
            @PathVariable(value = "app_service_id") Long appServiceId) {
        return Optional.ofNullable(
                orgAppMarketService.listServiceVersionsByAppServiceId(appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.services_version.listByAppServiceId"));
    }


    /**
     * @param appMarketUploadVO
     * @return
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "应用上传")
    @PostMapping("/upload")
    public ResponseEntity uploadAPP(
            @ApiParam(value = "应用信息", required = true)
            @RequestBody AppMarketUploadPayload appMarketUploadVO) {
        orgAppMarketService.uploadAPP(appMarketUploadVO);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * @param appMarketFixVersionPayload
     * @return
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "应用上传 版本修复")
    @PostMapping("/upload_fix_version")
    public ResponseEntity uploadAPPFixVersion(
            @ApiParam(value = "应用信息", required = true)
            @RequestBody AppMarketFixVersionPayload appMarketFixVersionPayload) {
        orgAppMarketService.uploadAPPFixVersion(appMarketFixVersionPayload);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * @param applicationPayload
     * @return
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "应用下载")
    @PostMapping("/download")
    public ResponseEntity downLoadApp(
            @ApiParam(value = "应用信息", required = true)
            @RequestBody AppMarketDownloadPayload applicationPayload) {
        orgAppMarketService.downLoadApp(applicationPayload);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}

