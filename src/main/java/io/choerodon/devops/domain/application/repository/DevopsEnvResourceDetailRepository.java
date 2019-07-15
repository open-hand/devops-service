package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvResourceDetailE;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceDetailRepository {
    DevopsEnvResourceDetailE baseCreate(DevopsEnvResourceDetailE devopsEnvResourceDetailE);

    DevopsEnvResourceDetailE baesQueryByMessageId(Long messageId);

    void baseUpdate(DevopsEnvResourceDetailE devopsEnvResourceDetailE);
}
