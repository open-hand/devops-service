package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.polaris.PolarisResponsePayloadVO;
import io.choerodon.devops.app.service.PolarisScanningService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 接受agent进行polaris扫描的结果数据
 *
 * @author zmf
 * @since 2021/1/26
 */
@RestController
@RequestMapping("/v1/polaris")
public class PolarisCallbackController {
    @Autowired
    private PolarisScanningService polarisScanningService;

    @Permission(permissionPublic = true)
    @ApiOperation("接收polaris扫描的结果")
    @PostMapping
    public ResponseEntity<Void> finishScanning(@RequestParam("token") String token,
                                               @RequestParam("cluster_id") Long clusterId,
                                               @RequestBody PolarisResponsePayloadVO message) {
        polarisScanningService.handleAgentPolarisMessageFromHttp(token, clusterId, message);
        return Results.success();
    }
}
