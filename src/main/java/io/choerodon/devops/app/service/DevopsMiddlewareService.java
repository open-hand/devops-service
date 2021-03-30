package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.MiddlewareRedisEnvDeployVO;
import io.choerodon.devops.api.vo.MiddlewareRedisHostDeployVO;
import io.choerodon.devops.app.eventhandler.payload.DevopsMiddlewareRedisDeployPayload;

public interface DevopsMiddlewareService {
    AppServiceInstanceVO envDeployForRedis(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO);

    void hostDeployForRedis(Long projectId, MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO);

    void hostDeployForRedis(DevopsMiddlewareRedisDeployPayload devopsMiddlewareRedisDeployPayload);

    void saveMiddlewareInfo(Long projectId, String name, String type, String mode, String version, String hostIds, String configuration);

    AppServiceInstanceVO updateRedisInstance(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO);

    MiddlewareRedisEnvDeployVO queryRedisConfig(Long projectId, Long appServiceInstanceId, Long marketDeployObjectId);

    void updateMiddlewareStatus();
}
