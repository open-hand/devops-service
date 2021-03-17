package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.MiddlewareRedisEnvDeployVO;
import io.choerodon.devops.api.vo.MiddlewareRedisHostDeployVO;

public interface DevopsMiddlewareService {
    AppServiceInstanceVO envDeployForRedis(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO);

    void hostDeployForRedis(Long projectId, MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO);

    void saveMiddlewareInfo(Long projectId, String name, String type, String mode, String version, String hostIds, String configuration);

    AppServiceInstanceVO updateRedisInstance(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO);
}
