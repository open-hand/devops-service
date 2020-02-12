package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsRegistrySecretDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsRegistrySecretService {
    /**
     * 删除RegistrySecret纪录
     *
     * @param envId 环境id
     */
    void deleteByEnvId(Long envId);

    DevopsRegistrySecretDTO baseCreate(DevopsRegistrySecretDTO devopsRegistrySecretDTO);

    DevopsRegistrySecretDTO baseQuery(Long devopsRegistrySecretId);

    DevopsRegistrySecretDTO baseUpdate(DevopsRegistrySecretDTO devopsRegistrySecretDTO);

    void baseUpdateStatus(Long id, Boolean status);

    /**
     * 查询集群下的namespace中是否存在对应配置的secret
     *
     * @param clusterId 集群id
     * @param namespace 环境code
     * @param configId  配置id
     * @return 查询结果
     */
    DevopsRegistrySecretDTO baseQueryByClusterIdAndNamespace(Long clusterId, String namespace, Long configId);

    List<DevopsRegistrySecretDTO> baseListByConfig(Long configId);

    /**
     * 查询集群下的namespace中是否存在对应name的secret
     *
     * @param clusterId 集群id
     * @param namespace 环境code
     * @param name      secret名称
     * @return 查询结果
     */
    DevopsRegistrySecretDTO baseQueryByClusterAndNamespaceAndName(Long clusterId, String namespace, String name);

}
