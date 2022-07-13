package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.HelmConfigVO;

public interface DevopsHelmConfigService {
    List<HelmConfigVO> listHelmConfig(Long projectId);
}
