package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiTplHostDeployInfoCfgDTO;

public interface DevopsCiTplHostDeployInfoService {

    CiTplHostDeployInfoCfgDTO selectByPrimaryKey(Long configId);
}
