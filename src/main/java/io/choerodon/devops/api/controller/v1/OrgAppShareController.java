package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.AppVersionAndValueDTO;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;
import io.choerodon.devops.app.service.AppShareService;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:30 2019/6/28
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/organizations/apps_share")
public class OrgAppShareController {
    @Autowired
    private AppShareService appShareService;

    /**
     * 查询所有已发布的应用
     *
     * @param publishLevel publish_level
     * @param pageRequest  分页参数
     * @param searchParam  搜索参数
     * @return list of ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "查询所有已发布的应用")
    @CustomPageRequest
    @PostMapping(value = "/listByOptions")
    public ResponseEntity<PageInfo<ApplicationReleasingDTO>> pageListMarketAppsByProjectId(
            @ApiParam(value = "发布层级")
            @RequestParam(value = "publish_level", required = false) String publishLevel,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                appShareService.listMarketAppsBySite(publishLevel, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.share.applications.by.site"));
    }

    /**
     * 根据shareId获取应用详情
     *
     * @return list of ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "根据shareId获取应用详情")
    @GetMapping(value = "/app_detail")
    public ResponseEntity<ApplicationReleasingDTO> getAppDetailByShareId(
            @ApiParam(value = "shareId")
            @RequestParam(value = "share_id") Long shareId) {
        return Optional.ofNullable(
                appShareService.getAppDetailByShareId(shareId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.detail.by.share.id"));
    }

    /**
     * 根据shareId更新应用共享
     *
     * @return list of ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "根据shareId更新应用共享")
    @PutMapping(value = "/update")
    public ResponseEntity updateByShareId(
            @ApiParam(value = "shareId")
            @RequestParam(value = "share_id") Long shareId,
            @ApiParam(value = "是否收费")
            @RequestParam(value = "is_free") Boolean isFree) {
        appShareService.updateByShareId(shareId, isFree);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 批量发布应用到平台
     *
     * @param releasingDTOList 发布应用的信息
     * @return Long
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "批量发布应用到平台")
    @PostMapping(value = "/batch_release")
    public ResponseEntity<List<Long>> batchRelease(
            @ApiParam(value = "发布应用的信息", required = true)
            @RequestBody List<ApplicationReleasingDTO> releasingDTOList) {
        return Optional.ofNullable(
                appShareService.batchRelease(releasingDTOList))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.batch.release"));
    }

    /**
     * 根据Ids获取应用详情
     *
     * @param shareIds 订阅Id
     * @return Long
     */
    @Permission(type = ResourceType.SITE)
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
                appShareService.getAppsDetail(pageRequest, params, shareIds))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.app.detail.by.shareId"));
    }

    /**
     * 根据应用Id获取已发布版本
     *
     * @param appId 应用Id
     * @return Long
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "根据应用Id获取已发布版本")
    @CustomPageRequest
    @PostMapping(value = "/list_versions")
    public ResponseEntity<PageInfo<ApplicationVersionRepDTO>> getVersionsByAppId(
            @ApiParam(value = "应用Id")
            @RequestParam(value = "app_id") Long appId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestParam(required = false) String version) {
        return Optional.ofNullable(
                appShareService.getVersionsByAppId(appId, pageRequest, version))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.versions.by.appId"));
    }

    /**
     * 根据版本Id获取values和chart
     *
     * @param versionId 版本Id
     * @return Long
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "根据版本Id获取values和chart")
    @GetMapping(value = "/values")
    public ResponseEntity<AppVersionAndValueDTO> getValuesAndChart(
            @ApiParam(value = "应用Id")
            @RequestParam(value = "version_id") Long versionId) {
        return Optional.ofNullable(
                appShareService.getValuesAndChart(versionId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.get.values.chart"));
    }

}

