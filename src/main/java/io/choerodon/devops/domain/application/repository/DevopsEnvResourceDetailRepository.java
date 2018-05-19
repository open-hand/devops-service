package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsEnvResourceDetailE;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceDetailRepository {
    DevopsEnvResourceDetailE create(DevopsEnvResourceDetailE devopsEnvResourceDetailE);

    DevopsEnvResourceDetailE query(Long messageId);

    void update(DevopsEnvResourceDetailE devopsEnvResourceDetailE);
}
