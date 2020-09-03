package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CountVO;

import java.util.Map;

public interface DevopsProjectOverview {
    Map<String, Long> getEnvStatusCount(Long projectId);

    Map<String, Long> getAppServiceStatusCount(Long projectId);

    CountVO getCommitCount(Long projectId);

    CountVO getDeployCount(Long projectId);

    CountVO getCiCount(Long projectId);
}
