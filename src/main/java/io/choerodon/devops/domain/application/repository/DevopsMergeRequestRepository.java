package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;

public interface DevopsMergeRequestRepository {
    Integer create(DevopsMergeRequestE devopsMergeRequestE);
}
