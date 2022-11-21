package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiTplApiTestInfoCfgDTO;

public interface DevopsCiTplApiTestInfoCfgService {
    CiTplApiTestInfoCfgDTO selectByPrimaryKey(Long configId);
}
