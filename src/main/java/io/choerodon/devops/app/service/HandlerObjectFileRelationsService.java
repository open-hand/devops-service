package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.kubernetes.client.models.V1Endpoints;


public interface HandlerObjectFileRelationsService<T> {

    void handlerRelations(Map<String, String> objectPath,
                          List<DevopsEnvFileResourceDTO> beforeSync,
                          List<T> ts,
                          List<V1Endpoints> v1Endpoints,
                          Long envId, Long projectId, String path, Long userId);

}
