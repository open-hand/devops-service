package io.choerodon.devops.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.ComponentReleaseService;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;

/**
 * @author zmf
 * @since 10/29/19
 */
@Service
public class ComponentReleaseServiceImpl implements ComponentReleaseService {
    @Override
    public AppServiceInstanceDTO createReleaseForPrometheus(DevopsPrometheusDTO devopsPrometheusDTO) {
        // TODO implement
        return null;
    }
}
