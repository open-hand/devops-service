package io.choerodon.devops.api.controller.v1;

import java.util.Optional;
import java.util.Set;

import com.github.pagehelper.PageInfo;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.ClusterOverViewVO;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.annotation.Permission;
import org.springframework.data.domain.Pageable;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AppServiceVO;
import io.choerodon.devops.app.service.AppServiceService;

/**
 * @author zhaotianxin
 * @since 2019/9/20
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}")
public class DevopsOrganizationController {
    @Autowired
    AppServiceService applicationServiceService;
    @Autowired
    private DevopsCheckLogService devopsCheckLogService;
    @Autowired
    private DevopsClusterService devopsClusterService;

    @ApiOperation(value = "批量查询应用服务")
    @PostMapping(value = "/app_service/list_app_service_ids")
    public ResponseEntity<PageInfo<AppServiceVO>> batchQueryAppService(
            @ApiParam(value = "组织ID")
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "应用服务Ids")
            @RequestParam(value = "ids") Set<Long> ids,
            @ApiParam(value = "是否分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "分页参数")
            @ApiIgnore Pageable pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(
                applicationServiceService.listAppServiceByIds(null, ids, doPage, true, pageable, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.service.ids"));
    }

    @Permission(permissionPublic = true)
    @GetMapping("/syncOrgRoot")
    @ApiOperation("手动同步组织root")
    public void syncOrgRoot() {
        devopsCheckLogService.checkLog("0.21.0");
    }

    @GetMapping("/cluster/overview")
    @ApiOperation("组织层概览，返回集群的概览")
    public ResponseEntity<ClusterOverViewVO> clusterOverview(
            @PathVariable(name = "organization_id") Long organizationId) {
        return Optional.ofNullable(devopsClusterService.getOrganizationClusterOverview(organizationId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.cluster.org.id"));
    }

}
