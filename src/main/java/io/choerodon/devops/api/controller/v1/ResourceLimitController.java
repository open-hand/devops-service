package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.annotation.Permission;
import io.choerodon.devops.api.vo.ResourceLimitVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈功能简述〉
 * 〈组织资源限制Controller〉
 *
 * @author wanghao
 * @Date 2020/3/26 21:46
 */
@RestController
@RequestMapping("v1/organizations/resource_limit")
public class ResourceLimitController {

    @Value("${choerodon.organization.resourceLimit.appSvcMaxNumber:100}")
    private Integer appSvcMaxNumber;
    @Value("${choerodon.organization.resourceLimit.clusterMaxNumber:10}")
    private Integer clusterMaxNumber;
    @Value("${choerodon.organization.resourceLimit.envMaxNumber:30}")
    private Integer envMaxNumber;

    @Permission(permissionWithin = true)
    @ApiOperation(value = "查询资源限制值")
    @GetMapping
    public ResponseEntity<ResourceLimitVO> queryResourceLimit() {
        ResourceLimitVO resourceLimitVO = new ResourceLimitVO();
        resourceLimitVO.setAppSvcMaxNumber(appSvcMaxNumber);
        resourceLimitVO.setClusterMaxNumber(clusterMaxNumber);
        resourceLimitVO.setEnvMaxNumber(envMaxNumber);
        return ResponseEntity.ok(resourceLimitVO);
    }
}
