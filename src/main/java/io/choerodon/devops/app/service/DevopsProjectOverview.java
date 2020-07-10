package io.choerodon.devops.app.service;

import java.util.Map;

public interface DevopsProjectOverview {
    Map<String, Long> getEnvStatusCount(Long projectId);

    Map<String, Long> getAppServiceStatusCount(Long projectId);

    Map<String, Long> getCommitCount(Long projectId);

    Map<String, Long> getDeployCount(Long projectId);
}
