package io.choerodon.devops.domain.application.repository;

import java.util.List;


import io.choerodon.devops.api.vo.iam.entity.DevopsCheckLogE;
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;

import io.choerodon.devops.infra.dto.DevopsProjectDO;

public interface DevopsCheckLogRepository {

    void create(DevopsCheckLogE devopsCheckLogE);

    List<DevopsProjectDTO> queryNonEnvGroupProject();

    void syncCommandId();

    void syncCommandVersionId();
}
