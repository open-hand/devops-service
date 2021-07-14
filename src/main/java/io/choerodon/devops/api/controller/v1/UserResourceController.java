package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ClusterDetailResourceVO;
import io.choerodon.devops.api.vo.GeneralResourceVO;
import io.choerodon.devops.api.vo.HostDetailResourceVO;
import io.choerodon.devops.app.service.UserResourceService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wxx
 * @since 2021/7/13
 */
@RestController
@RequestMapping("/v1/organizations/{organization_id}/resource")
public class UserResourceController {
    @Autowired
    private UserResourceService userResourceService;


    @ApiOperation("查询上下文用户在指定组织下的资源概览")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/general")
    @CustomPageRequest
    public ResponseEntity<GeneralResourceVO> queryGeneral(@PathVariable("organization_id") Long organizationId) {
        return Results.success(userResourceService.queryGeneral(organizationId));
    }

    @ApiOperation("查询上下文用户在指定组织下的主机资源详情")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/host")
    @CustomPageRequest
    public ResponseEntity<List<HostDetailResourceVO>> queryHostResource(@PathVariable("organization_id") Long organizationId) {
        return Results.success(userResourceService.queryHostResource(organizationId));
    }

    @ApiOperation("查询上下文用户在指定组织下的集群资源详情")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/cluster")
    @CustomPageRequest
    public ResponseEntity<List<ClusterDetailResourceVO>> queryClusterResource(@PathVariable("organization_id") Long organizationId) {
        return Results.success(userResourceService.queryClusterResource(organizationId));
    }

}
