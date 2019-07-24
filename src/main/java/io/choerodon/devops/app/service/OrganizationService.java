package io.choerodon.devops.app.service;

import io.choerodon.devops.app.eventhandler.payload.OrganizationEventPayload;

public interface OrganizationService {

    void create(OrganizationEventPayload organizationEventPayload);
}
