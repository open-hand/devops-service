package io.choerodon.devops.app.service;

import io.choerodon.devops.domain.application.event.OrganizationEventPayload;

public interface OrganizationService {

    void create(OrganizationEventPayload organizationEventPayload);
}
