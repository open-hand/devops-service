package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.ClusterOverViewVO;
import io.choerodon.devops.app.service.DevopsClusterService;

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

    @Permission(type = ResourceType.SITE, roles = InitRoleCode.SITE_ADMINISTRATOR)
    @GetMapping("/clusters/overview")
    @ApiOperation("查询平台层的集群概览信息")
    public ResponseEntity<ClusterOverViewVO> getSiteClusterOverview() {
        return new ResponseEntity<>(devopsClusterService.getSiteClusterOverview(), HttpStatus.OK);
    }
}
