package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.DevopsMiddlewareDeployPayload;

public interface DevopsMiddlewareService {
    AppServiceInstanceVO envDeployForRedis(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO);

    void hostDeployForRedis(Long projectId, MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO);

    void hostDeployForRedis(DevopsMiddlewareDeployPayload devopsMiddlewareDeployPayload);

    void saveMiddlewareInfo(Long projectId, String name, String type, String mode, String version, String hostIds, String configuration);

    AppServiceInstanceVO envDeployForMySql(Long projectId, MiddlewareMySqlEnvDeployVO middlewareMySqlEnvDeployVO);

    AppServiceInstanceVO updateRedisInstance(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO);

    MiddlewareRedisEnvDeployVO queryRedisConfig(Long projectId, Long appServiceInstanceId, Long marketDeployObjectId);

    void updateMiddlewareStatus();

    void hostDeployForMySql(Long projectId, MiddlewareMySqlHostDeployVO middlewareMySqlHostDeployVO);

    void hostDeployForMySql(DevopsMiddlewareDeployPayload devopsMiddlewareDeployPayload);
}
