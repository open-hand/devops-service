package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppServiceRepVO;
import io.choerodon.devops.api.vo.ClusterOverViewVO;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 放置一些site级别的API接口
 *
 * @author zmf
 * @since 20-3-17
 */
@RequestMapping("/v1")
@RestController
public class DevOpsSiteLevelResourceController {
    @Lazy
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private AppServiceService appServiceService;

    @Permission(level = ResourceLevel.SITE, roles = InitRoleCode.SITE_ADMINISTRATOR)
    @GetMapping("/clusters/overview")
    @ApiOperation("查询平台层的集群概览信息")
    public ResponseEntity<ClusterOverViewVO> getSiteClusterOverview() {
        return new ResponseEntity<>(devopsClusterService.getSiteClusterOverview(), HttpStatus.OK);
    }

    /**
     * 根据多个用户Id查询存在的多个用户信息
     */
    @Permission(level = ResourceLevel.SITE, permissionWithin = true)
    @ApiOperation(value = "根据多个用户Id查询存在的多个用户信息")
    @PostMapping("/users/list_by_ids")
    public ResponseEntity<List<UserAttrVO>> listByUserIds(
            @Encrypt
            @ApiParam(value = "用户id", required = true)
            @RequestBody Set<Long> iamUserIds) {
        return new ResponseEntity<>(userAttrService.listByUserIds(iamUserIds), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiOperation(value = "批量查询应用服务")
    @PostMapping(value = "/app_service/list_app_service_by_ids")
    public ResponseEntity<Page<AppServiceRepVO>> batchQueryAppService(
            @Encrypt
            @ApiParam(value = "应用服务Ids, 不能为空，也不能为空数组", required = true)
            @RequestBody Set<Long> ids) {
        return new ResponseEntity<>(appServiceService.listAppServiceByIds(ids, false, null, null), HttpStatus.OK);
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "根据一组Gitlab用户id查询用户信息")
    @PostMapping(value = "/users/list_by_gitlab_user_ids")
    public ResponseEntity<List<UserAttrVO>> listUsersByGitlabUserIds(
            @ApiParam(value = "Gitlab用户id", required = true)
            @RequestBody Set<Long> gitlabUserIds) {
        return new ResponseEntity<>(userAttrService.listUsersByGitlabUserIds(gitlabUserIds), HttpStatus.OK);
    }
}
