package io.choerodon.devops.api.controller.v1;

import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.MiddlewareRedisEnvDeployVO;
import io.choerodon.devops.api.vo.MiddlewareRedisHostDeployVO;
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
                                                                     @RequestBody @Valid MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {
        middlewareRedisEnvDeployVO.setCommandType(CommandType.CREATE.getType());
        middlewareRedisEnvDeployVO.setSource(AppServiceInstanceSource.MIDDLEWARE.getValue());
        return ResponseEntity.ok(middlewareService.envDeployForRedis(projectId, middlewareRedisEnvDeployVO));
    }

    /**
     * 更新redis中间件实例
     */
    @ApiOperation(value = "环境部署更新实例")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/redis/{instance_id}")
    public ResponseEntity<AppServiceInstanceVO> updateRedisInstance(@PathVariable("project_id") Long projectId,
                                                                     @Encrypt
                                                                     @ApiParam(value = "实例id",required = true)
                                                                     @PathVariable("instance_id")Long instanceId,
                                                                     @RequestBody @Valid MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {
        middlewareRedisEnvDeployVO.setCommandType(CommandType.UPDATE.getType());
        middlewareRedisEnvDeployVO.setInstanceId(instanceId);
        middlewareRedisEnvDeployVO.setSource(AppServiceInstanceSource.MIDDLEWARE.getValue());
        return ResponseEntity.ok(middlewareService.updateRedisInstance(projectId, middlewareRedisEnvDeployVO));
    }


    /**
     * 主机部署
     */
    @ApiOperation(value = "主机部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/redis/deploy/host")
    public ResponseEntity<Void> deployForHost(@PathVariable("project_id")Long projectId,
                                              @RequestBody @Valid MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO) {
        middlewareService.hostDeployForRedis(projectId,middlewareRedisHostDeployVO);
        return ResponseEntity.noContent().build();
    }
}
