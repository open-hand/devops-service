package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileLogE;

public interface DevopsEnvFileLogRepository {

    void create(DevopsEnvFileLogE devopsEnvFileLogE);

    List<DevopsEnvFileLogE> listByEnvId(Long envId);

}
