package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dataobject.DevopsCheckLogDO;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCheckLogMapper extends BaseMapper<DevopsCheckLogDO> {
    List<DevopsProjectDO> queryNonEnvGroupProject();

    void syncCommandId();

    void syncCommandVersionId();
}
