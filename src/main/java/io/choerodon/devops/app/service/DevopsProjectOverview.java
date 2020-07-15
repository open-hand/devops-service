package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

public interface DevopsProjectOverview {
    Map<String, Long> getEnvStatusCount(Long projectId);

    Map<String, Long> getAppServiceStatusCount(Long projectId);

    Map<String, List<Object>> getCommitCount(Long projectId);

    Map<String, List<Object>> getDeployCount(Long projectId);
}
