package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.AppServiceVO;
import io.choerodon.devops.api.vo.ClusterOverViewVO;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.app.service.UserAttrService;

/**
 * 放置一些site级别的API接口
 *
 * @author zmf
 * @since 20-3-17
 */
@RequestMapping("/v1")
@RestController
public class DevOpsSiteLevelResourceController {
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private AppServiceService appServiceService;

    @Permission(type = ResourceType.SITE, roles = InitRoleCode.SITE_ADMINISTRATOR)
    @GetMapping("/clusters/overview")
    @ApiOperation("查询平台层的集群概览信息")
    public ResponseEntity<ClusterOverViewVO> getSiteClusterOverview() {
        return new ResponseEntity<>(devopsClusterService.getSiteClusterOverview(), HttpStatus.OK);
    }

    /**
     * 根据多个用户Id查询存在的多个用户信息
     */
    @Permission(type = ResourceType.SITE, permissionWithin = true)
    @ApiOperation(value = "根据多个用户Id查询存在的多个用户信息")
    @GetMapping("/users/list_by_ids")
    public ResponseEntity<List<UserAttrVO>> listByUserIds(
            @ApiParam(value = "用户id", required = true)
            @RequestParam(value = "user_ids") Set<Long> iamUserIds) {
        return Optional.ofNullable(userAttrService.listByUserIds(iamUserIds))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.user.get"));
    }


    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation(value = "批量查询应用服务")
    @GetMapping(value = "/app_service/list_app_service_by_ids")
    public ResponseEntity<PageInfo<AppServiceVO>> batchQueryAppService(
            @ApiParam(value = "应用服务Ids, 不能为空，也不能为空数组", required = true)
            @RequestParam(value = "ids") Set<Long> ids) {
        return Optional.ofNullable(
                appServiceService.listAppServiceByIds(null, ids, false, false, null, null))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.list.app.service.ids"));
    }
}
