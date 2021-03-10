package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.MiddlewareRedisDeployVO;
import io.choerodon.devops.app.service.DevopsMiddlewareService;
import io.choerodon.devops.infra.enums.AppServiceInstanceSource;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/middleware")
public class DevopsMiddlewareController {

    @Autowired
    private DevopsMiddlewareService middlewareService;

    /**
     * 环境部署
     */
    @ApiOperation(value = "环境部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/redis/deploy/env")
    public ResponseEntity<AppServiceInstanceVO> deployForEnvironment(@PathVariable("project_id") Long projectId,
                                                                     @RequestBody MiddlewareRedisDeployVO middlewareRedisDeployVO) {
        middlewareRedisDeployVO.setCommandType(CommandType.CREATE.getType());
        middlewareRedisDeployVO.setSource(AppServiceInstanceSource.MIDDLEWARE.getValue());
        return ResponseEntity.ok(middlewareService.deployForRedis(projectId, middlewareRedisDeployVO));
    }


    /**
     * 主机部署
     */
    @ApiOperation(value = "主机部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/redis/deploy/host")
    public void deployForHost() {
        // TODO 完成主机部署逻辑
    }
}
