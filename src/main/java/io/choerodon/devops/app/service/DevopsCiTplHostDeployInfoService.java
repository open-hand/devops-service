package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiTplHostDeployInfoDTO;

public interface DevopsCiTplHostDeployInfoService {

    DevopsCiTplHostDeployInfoDTO selectByPrimaryKey(Long configId);
}
