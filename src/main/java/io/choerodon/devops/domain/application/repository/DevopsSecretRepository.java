package io.choerodon.devops.domain.application.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsSecretE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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

    Page<DevopsSecretE> listByOption(Long envId, PageRequest pageRequest, String params);
}
