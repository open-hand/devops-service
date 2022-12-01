package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiApiTestInfoDTO;

public interface DevopsCiApiTestInfoService {
    void insert(DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO);

    DevopsCiApiTestInfoDTO selectByPrimaryKey(Long id);

    DevopsCiApiTestInfoDTO selectById(Long id);

    void deleteConfigByPipelineId(Long ciPipelineId);
}
