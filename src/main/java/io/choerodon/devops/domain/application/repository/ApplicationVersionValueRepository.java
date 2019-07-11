package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.ApplicationVersionValueE;

public interface ApplicationVersionValueRepository {

    ApplicationVersionValueE create(ApplicationVersionValueE applicationVersionValueE);

    ApplicationVersionValueE query(Long appVersionValueId);

}
