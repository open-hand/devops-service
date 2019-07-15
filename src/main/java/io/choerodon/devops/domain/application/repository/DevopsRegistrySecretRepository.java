package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsRegistrySecretE;
import io.choerodon.devops.infra.dto.DevopsRegistrySecretDTO;

/**
 * Created by Sheep on 2019/3/14.
 */
public interface DevopsRegistrySecretRepository {

    DevopsRegistrySecretDTO baseCreate(DevopsRegistrySecretDTO devopsRegistrySecretDTO);

    DevopsRegistrySecretDTO baseQuery(Long devopsRegistrySecretId);

    DevopsRegistrySecretDTO baseUpdate(DevopsRegistrySecretDTO devopsRegistrySecretDTO);

    DevopsRegistrySecretDTO baseQueryByEnvAndId(String namespace, Long configId);

    List<DevopsRegistrySecretDTO> baseListByConfigId(Long configId);

    DevopsRegistrySecretDTO baseQueryByEnvAndName(Long envId, String name);

}
