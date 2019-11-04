package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsPvcReqVO;

public interface DevopsPvcService {
    void create(Long projectId, DevopsPvcReqVO devopsPvcReqVO);

    void baseCheckName(String PvcName, Long envId);
}
