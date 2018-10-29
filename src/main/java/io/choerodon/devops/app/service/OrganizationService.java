package io.choerodon.devops.app.service;

import io.choerodon.devops.api.dto.RegisterOrganizationDTO;
import io.choerodon.devops.domain.application.event.OrganizationEventPayload;

public interface OrganizationService {

    void create(OrganizationEventPayload organizationEventPayload);

    void registerOrganization(RegisterOrganizationDTO registerOrganizationDTO);
}
