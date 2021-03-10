package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.MiddlewareRedisDeployVO;

public interface DevopsMiddlewareService {
    AppServiceInstanceVO deployForRedis(Long projectId, MiddlewareRedisDeployVO middlewareRedisDeployVO);
}
