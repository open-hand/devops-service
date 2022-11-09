package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiApiTestInfoVO;

public interface DevopsCiApiTestInfoService {
    DevopsCiApiTestInfoVO selectByPrimaryKey(Long id);
}
