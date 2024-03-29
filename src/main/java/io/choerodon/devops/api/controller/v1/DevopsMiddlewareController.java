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
import io.choerodon.devops.infra.enums.AppSourceType;
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
        middlewareRedisEnvDeployVO.setSource(AppSourceType.MIDDLEWARE.getValue());
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
        middlewareMySqlEnvDeployVO.setSource(AppSourceType.MIDDLEWARE.getValue());
        return ResponseEntity.ok(middlewareService.envDeployForMySql(projectId, middlewareMySqlEnvDeployVO));
    }

    /**
     * MySQL主机部署部署
     */

    @ApiOperation(value = "MySQL主机部署部署")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/mysql/deploy/host")
    public ResponseEntity<Void> hostDeployForMySql(@PathVariable("project_id") Long projectId,
                                                   @RequestBody @Valid MiddlewareMySqlHostDeployVO middlewareMySqlHostDeployVO) {
        middlewareService.hostDeployForMySql(projectId, middlewareMySqlHostDeployVO);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新中间件实例
     */
    @ApiOperation(value = "环境部署更新实例")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/{instance_id}")
    public ResponseEntity<AppServiceInstanceVO> updateMiddlewareInstance(@PathVariable("project_id") Long projectId,
                                                                         @Encrypt
                                                                         @ApiParam(value = "实例id", required = true)
                                                                         @PathVariable("instance_id") Long instanceId,
                                                                         @RequestBody MarketInstanceCreationRequestVO marketInstanceCreationRequestVO) {
        marketInstanceCreationRequestVO.setCommandType(CommandType.UPDATE.getType());
        marketInstanceCreationRequestVO.setInstanceId(instanceId);
        marketInstanceCreationRequestVO.setSource(AppSourceType.MIDDLEWARE.getValue());
        return ResponseEntity.ok(middlewareService.updateMiddlewareInstance(projectId, marketInstanceCreationRequestVO));
    }

    /**
     * 更新中间件主机实例名称
     */
    @ApiOperation(value = "更新中间件主机实例名称")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/host/update")
    public ResponseEntity<Void> updateAppName(@PathVariable("project_id") Long projectId,
                                              @RequestBody MiddlewareHostInstanceVO middlewareHostInstanceVO) {
        middlewareService.updateHostInstance(projectId, middlewareHostInstanceVO);
        return ResponseEntity.noContent().build();
    }
}
