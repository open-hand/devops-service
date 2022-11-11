package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiTplApiTestInfoCfgDTO;

public interface DevopsCiTplApiTestInfoCfgService {
    DevopsCiTplApiTestInfoCfgDTO selectByPrimaryKey(Long configId);
}
