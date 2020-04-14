package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCheckLogDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;

public interface DevopsCheckLogMapper extends BaseMapper<DevopsCheckLogDTO> {
    List<DevopsProjectDTO> queryNonEnvGroupProject();

    void syncCommandId();

    void syncCommandVersionId();
}
