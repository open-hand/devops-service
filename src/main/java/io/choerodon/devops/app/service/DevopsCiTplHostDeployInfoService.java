package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiTplHostDeployInfoCfgDTO;

public interface DevopsCiTplHostDeployInfoService {

    DevopsCiTplHostDeployInfoCfgDTO selectByPrimaryKey(Long configId);
}
