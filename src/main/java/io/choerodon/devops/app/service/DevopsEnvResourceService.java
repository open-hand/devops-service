package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.dto.DevopsEnvResourceDTO;
import io.choerodon.devops.api.dto.InstanceEventDTO;

/**
 * Created by younger on 2018/4/25.
 */
public interface DevopsEnvResourceService {
    DevopsEnvResourceDTO listResources(Long instanceId);

    List<InstanceEventDTO> listInstancePodEvent(Long instanceId);
}
