package io.choerodon.devops.api.controller.v1;

import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
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
     * redis环境部署
     */
    @ApiOperation(value = "redis环境部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/redis/deploy/env")
    public ResponseEntity<AppServiceInstanceVO> envDeployForRedis(@PathVariable("project_id") Long projectId,
                                                                  @RequestBody @Valid MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {
        middlewareRedisEnvDeployVO.setCommandType(CommandType.CREATE.getType());
        middlewareRedisEnvDeployVO.setSource(AppServiceInstanceSource.MIDDLEWARE.getValue());
        return ResponseEntity.ok(middlewareService.envDeployForRedis(projectId, middlewareRedisEnvDeployVO));
    }

    /**
     * 查询已部署redis中间件实例配置
     */
    @ApiOperation(value = "查询已部署redis中间件实例配置")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/redis/config")
    public ResponseEntity<MiddlewareRedisEnvDeployVO> queryRedisConfig(@PathVariable("project_id") Long projectId,
                                                                       @RequestParam("app_service_instance_id") Long appServiceInstanceId,
                                                                       @RequestParam("market_deploy_object_id") Long marketDeployObjectId) {
        return ResponseEntity.ok(middlewareService.queryRedisConfig(projectId, appServiceInstanceId, marketDeployObjectId));
    }

    /**
     * 更新redis中间件实例
     */
    @ApiOperation(value = "环境部署更新实例")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/redis/{instance_id}")
    public ResponseEntity<AppServiceInstanceVO> updateRedisInstance(@PathVariable("project_id") Long projectId,
                                                                    @Encrypt
                                                                    @ApiParam(value = "实例id", required = true)
                                                                    @PathVariable("instance_id") Long instanceId,
                                                                    @RequestBody MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {
        middlewareRedisEnvDeployVO.setCommandType(CommandType.UPDATE.getType());
        middlewareRedisEnvDeployVO.setInstanceId(instanceId);
        middlewareRedisEnvDeployVO.setSource(AppServiceInstanceSource.MIDDLEWARE.getValue());
        return ResponseEntity.ok(middlewareService.updateRedisInstance(projectId, middlewareRedisEnvDeployVO));
    }


    /**
     * redis主机部署
     */
    @ApiOperation(value = "redis主机部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/redis/deploy/host")
    public ResponseEntity<Void> hostDeployForRedis(@PathVariable("project_id") Long projectId,
                                                   @RequestBody @Valid MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO) {
        middlewareService.hostDeployForRedis(projectId, middlewareRedisHostDeployVO);
        return ResponseEntity.noContent().build();
    }

    /**
     * MySQL环境部署
     */

    @ApiOperation(value = "MySQL环境部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/mysql/deploy/env")
    public ResponseEntity<AppServiceInstanceVO> envDeployForMySql(@PathVariable("project_id") Long projectId,
                                                                  @RequestBody @Valid MiddlewareMySqlEnvDeployVO middlewareMySqlEnvDeployVO) {
        middlewareMySqlEnvDeployVO.setCommandType(CommandType.CREATE.getType());
        middlewareMySqlEnvDeployVO.setSource(AppServiceInstanceSource.MIDDLEWARE.getValue());
        return ResponseEntity.ok(middlewareService.envDeployForMySql(projectId, middlewareMySqlEnvDeployVO));
    }

    /**
     * MySQL主机部署部署
     */

    @ApiOperation(value = "MySQL主机部署部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/mysql/deploy/env")
    public ResponseEntity<Void> hostDeployForMySql(@PathVariable("project_id") Long projectId,
                                                   @RequestBody @Valid MiddlewareMySqlHostDeployVO middlewareMySqlHostDeployVO) {
        middlewareService.hostDeployForMySql(projectId, middlewareMySqlHostDeployVO);
        return ResponseEntity.noContent().build();
    }
}
