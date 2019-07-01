package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
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
     * 平台下查询所有发布在平台层的应用
     *
     * @param pageRequest 分页参数
     * @param searchParam 搜索参数
     * @return list of ApplicationReleasingDTO
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "平台下查询所有发布在平台层的应用")
    @CustomPageRequest
    @PostMapping(value = "/listByOptions")
    public ResponseEntity<PageInfo<ApplicationReleasingDTO>> pageListMarketAppsByProjectId(
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(
                appShareService.listMarketAppsBySite(pageRequest, searchParam))
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
    @CustomPageRequest
    @PostMapping(value = "/app_detail")
    public ResponseEntity<ApplicationReleasingDTO> getAppDetailByShareId(
            @ApiParam(value = "shareId")
            @RequestParam(value = "share_id") Long shareId) {
        return Optional.ofNullable(
                appShareService.getAppDetailByShareId(shareId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.application.detail.by.share.id"));
    }

    /**
     * 批量发布应用到平台
     *
     * @param releasingDTOList 发布应用的信息
     * @return Long
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
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

//    /**
//     * 根据shareId获取应用详情
//     *
//     * @return list of ApplicationReleasingDTO
//     */
//    @Permission(type = ResourceType.SITE)
//    @ApiOperation(value = "根据shareId获取应用详情")
//    @CustomPageRequest
//    @PostMapping(value = "/app_detail")
//    public ResponseEntity<PageInfo<ApplicationReleasingDTO>> getAppDetailByShareId(
//            @ApiParam(value = "shareId")
//            @RequestParam(value = "share_id") Long shareId) {
//        return Optional.ofNullable(
//                appShareService.getAppDetailByShareId(shareId))
//                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
//                .orElseThrow(() -> new CommonException("error.application.detail.by.share.id"));
//    }


}
