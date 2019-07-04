package io.choerodon.devops.domain.application.repository;

import com.github.pagehelper.PageInfo;

import java.util.List;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsSecretE;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:04
 * Description:
 */
public interface DevopsSecretRepository {

    DevopsSecretE create(DevopsSecretE devopsSecretE);

    void update(DevopsSecretE devopsSecretE);

    void deleteSecret(Long secretId);

    void checkName(String name, Long envId);

    DevopsSecretE queryBySecretId(Long secretId);

    DevopsSecretE selectByEnvIdAndName(Long envId, String name);

    PageInfo<DevopsSecretE> listByOption(Long envId, PageRequest pageRequest, String params);

    PageInfo<DevopsSecretE> listSecretByApp(List<Long> secretIds, PageRequest pageRequest, String params);

    List<DevopsSecretE> listByEnv(Long envId);
}
