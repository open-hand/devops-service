package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsHelmConfigVO;

public interface DevopsHelmConfigService {
    List<DevopsHelmConfigVO> listHelmConfig(Long projectId);
}
