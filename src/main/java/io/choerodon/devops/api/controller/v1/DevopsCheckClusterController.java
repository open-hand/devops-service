package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author zhaotianxin
 * @since 2019/10/28
 */
@RestController
@RequestMapping(value = "/v1/checks")
public class DevopsCheckClusterController {
    @Lazy
    @Autowired
    private DevopsClusterService devopsClusterService;

    @Permission(level = ResourceLevel.SITE, permissionPublic = true)
    @ApiOperation(value = "验证用户是否拥有操作集群的权限")
    @GetMapping(value = "/clusterCheck")
    public ResponseEntity<Boolean> checkUserClusterPermission(
            @Encrypt
            @ApiParam(value = "集群ID", required = true)
            @RequestParam(value = "cluster_id") Long clusterId,
            @Encrypt
            @ApiParam(value = "用户ID", required = true)
            @RequestParam(value = "user_id") Long  userId) {
        return new ResponseEntity<>(devopsClusterService.checkUserClusterPermission(clusterId, userId), HttpStatus.OK);
    }
}
