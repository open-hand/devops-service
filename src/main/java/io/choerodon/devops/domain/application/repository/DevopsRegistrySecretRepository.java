package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsRegistrySecretE;

/**
 * Created by Sheep on 2019/3/14.
 */
public interface DevopsRegistrySecretRepository {

    DevopsRegistrySecretE create(DevopsRegistrySecretE devopsRegistrySecretE);

    DevopsRegistrySecretE query(Long devopsRegistrySecretId);

    DevopsRegistrySecretE update(DevopsRegistrySecretE devopsRegistrySecretE);

    DevopsRegistrySecretE queryByEnv(String namespace, Long configId);

    List<DevopsRegistrySecretE> listByConfig(Long configId);

    DevopsRegistrySecretE queryByName(Long envId, String name);

}
