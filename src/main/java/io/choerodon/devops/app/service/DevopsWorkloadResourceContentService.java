package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsWorkloadResourceContentDTO;

public interface DevopsWorkloadResourceContentService {
    DevopsWorkloadResourceContentDTO baseQuery(Long workLoadId, String type);

    void create(String type, Long workLoadId, String content);

    void update(String type, Long resourceId, String content);

    void deleteByResourceId(String type, Long workloadId);
}
