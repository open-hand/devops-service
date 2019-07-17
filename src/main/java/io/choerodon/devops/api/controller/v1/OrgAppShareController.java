package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.ApplicationShareService;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:30 2019/6/28
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/organizations/apps_share")
public class OrgAppShareController {
    @Autowired
    private ApplicationShareService applicationShareService;

    /**
     * 查询所有已发布的应用
     *
     * @param isSite      is_site
     * @param pageRequest 分页参数
     * @param searchParam 搜索参数
     * @return baseList of ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "查询所有已发布的应用")
    @CustomPageRequest
    @PostMapping(value = "/listByOptions")
    public ResponseEntity<PageInfo<ApplicationReleasingDTO>> pageListMarketAppsByProjectId(
            @ApiParam(value = "发布层级")
            @RequestParam(value = "is_site", required = false) Boolean isSite,
            @ApiParam(value = "是否收费")
            @RequestParam(value = "is_free", required = false) Boolean isFree,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                applicationShareService.listMarketAppsBySite(isSite, isFree, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.share.applications.by.site"));
    }

    /**
     * 根据shareId获取应用详情
     *
     * @return baseList of ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "根据shareId获取应用详情")
    @GetMapping(value = "/app_detail")
    public ResponseEntity<ApplicationReleasingDTO> getAppDetailByShareId(
            @ApiParam(value = "shareId")
            @RequestParam(value = "share_id") Long shareId) {
        return Optional.ofNullable(
                applicationShareService.getAppDetailByShareId(shareId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.detail.by.share.id"));
    }

    /**
     * 根据shareId更新应用共享
     *
     * @return baseList of ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "根据shareId更新应用共享")
    @PutMapping(value = "/update")
    public ResponseEntity updateByShareId(
            @ApiParam(value = "shareId")
            @RequestParam(value = "share_id") Long shareId,
            @ApiParam(value = "是否收费")
            @RequestParam(value = "is_free") Boolean isFree) {
        applicationShareService.updateByShareId(shareId, isFree);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 批量发布应用到平台
     *
     * @param releasingDTOList 发布应用的信息
     * @return Long
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "批量发布应用到平台")
    @PostMapping(value = "/batch_release")
    public ResponseEntity<List<Long>> batchRelease(
            @ApiParam(value = "发布应用的信息", required = true)
            @RequestBody List<ApplicationReleasingDTO> releasingDTOList) {
        return Optional.ofNullable(
                applicationShareService.batchRelease(releasingDTOList))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.batch.release"));
    }

    /**
     * 根据Ids获取应用详情
     *
     * @param shareIds 订阅Id
     * @return Long
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "根据Ids获取应用详情")
    @CustomPageRequest
    @PostMapping(value = "/details")
    public ResponseEntity<PageInfo<ApplicationReleasingDTO>> getAppsDetail(
            @ApiParam(value = "发布应用的信息", required = true)
            @RequestBody List<Long> shareIds,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestParam(required = false) String params) {
        return Optional.ofNullable(
                applicationShareService.getAppsDetail(pageRequest, params, shareIds))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.app.detail.by.shareId"));
    }

    /**
     * 根据应用Id获取已发布版本
     *
     * @param appId 应用Id
     * @return Long
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "根据应用Id获取已发布版本")
    @CustomPageRequest
    @PostMapping(value = "/list_versions")
    public ResponseEntity<PageInfo<ApplicationVersionRespVO>> getVersionsByAppId(
            @ApiParam(value = "应用Id")
            @RequestParam(value = "app_id") Long appId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestParam(required = false) String version) {
        return Optional.ofNullable(
                applicationShareService.getVersionsByAppId(appId, pageRequest, version))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.versions.by.appId"));
    }

    /**
     * 根据版本Id获取values和chart
     *
     * @param versionId 版本Id
     * @return Long
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "根据版本Id获取values和chart")
    @GetMapping(value = "/values")
    public ResponseEntity<AppVersionAndValueDTO> getValuesAndChart(
            @ApiParam(value = "应用Id")
            @RequestParam(value = "version_id") Long versionId) {
        return Optional.ofNullable(
                applicationShareService.getValuesAndChart(versionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.values.chart"));
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "token校验")
    @PostMapping(value = "/check_token")
    public ResponseEntity<AccessTokenCheckResultDTO> checkToken(
            @ApiParam(value = "token")
            @RequestBody AccessTokenDTO tokenDTO) {
        return Optional.ofNullable(
                applicationShareService.checkToken(tokenDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.check.access.token"));
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "token校验")
    @PostMapping(value = "/save_token")
    public ResponseEntity saveToken(
            @ApiParam(value = "token")
            @RequestBody AccessTokenDTO tokenDTO) {
        applicationShareService.saveToken(tokenDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

