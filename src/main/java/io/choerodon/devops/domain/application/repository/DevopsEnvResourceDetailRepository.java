package io.choerodon.devops.domain.application.repository;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceDetailRepository {
    DevopsEnvResourceDetailE baseCreate(DevopsEnvResourceDetailE devopsEnvResourceDetailE);

    DevopsEnvResourceDetailE baesQueryByMessageId(Long messageId);

    void baseUpdate(DevopsEnvResourceDetailE devopsEnvResourceDetailE);
}
