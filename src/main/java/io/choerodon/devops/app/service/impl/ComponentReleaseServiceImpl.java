package io.choerodon.devops.app.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.ComponentReleaseService;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;

/**
 * @author zmf
 * @since 10/29/19
 */
@Service
public class ComponentReleaseServiceImpl implements ComponentReleaseService {
    @Override
    public AppServiceInstanceDTO createReleaseForPrometheus(Map<String, String> values) {
        // TODO implement
        return null;
    }
}
