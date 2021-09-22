package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsHostAppInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsMiddlewareDTO;

public interface DevopsMiddlewareService {
    /**
     * 容器部署redis
     *
     * @param projectId
     * @param middlewareRedisEnvDeployVO
     * @return
     */
    AppServiceInstanceVO envDeployForRedis(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO);

    /**
     * 主机部署redis
     *
     * @param projectId
     * @param middlewareRedisHostDeployVO
     */
    void hostDeployForRedis(Long projectId, MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO);

    /**
     * 保存中间件信息
     *
     * @param projectId
     * @param instanceId
     * @param name
     * @param type
     * @param mode
     * @param version
     * @param hostIds
     * @param configuration
     * @return
     */
    DevopsMiddlewareDTO saveMiddlewareInfo(Long projectId, Long instanceId, String name, String type, String mode, String version, String hostIds, String configuration);

    /**
     * 容器部署mysql
     *
     * @param projectId
     * @param middlewareMySqlEnvDeployVO
     * @return
     */
    AppServiceInstanceVO envDeployForMySql(Long projectId, MiddlewareMySqlEnvDeployVO middlewareMySqlEnvDeployVO);

    /**
     * 更新容器实例
     *
     * @param projectId
     * @param marketInstanceCreationRequestVO
     * @return
     */
    AppServiceInstanceVO updateMiddlewareInstance(Long projectId, MarketInstanceCreationRequestVO marketInstanceCreationRequestVO);

    /**
     * 查询redis配置信息
     *
     * @param projectId
     * @param appServiceInstanceId
     * @param marketDeployObjectId
     * @return
     */
    MiddlewareRedisEnvDeployVO queryRedisConfig(Long projectId, Long appServiceInstanceId, Long marketDeployObjectId);

    /**
     * 主机部署mysql
     *
     * @param projectId
     * @param middlewareMySqlHostDeployVO
     */
    void hostDeployForMySql(Long projectId, MiddlewareMySqlHostDeployVO middlewareMySqlHostDeployVO);

    /**
     * 卸载中间件
     *
     * @param projectId
     * @param devopsHostAppInstanceDTO
     */
    void uninstallMiddleware(Long projectId, DevopsHostAppInstanceDTO devopsHostAppInstanceDTO);

    /**
     * 根据实例id删除中间
     * @param instanceId
     */
    void deleteByInstanceId(Long instanceId);

    /**
     * 根据实例id查询中间件
     * @param instanceId
     * @return
     */
    DevopsMiddlewareDTO queryByInstanceId(Long instanceId);
}
