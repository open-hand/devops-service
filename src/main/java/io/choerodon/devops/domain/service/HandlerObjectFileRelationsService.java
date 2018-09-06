package io.choerodon.devops.domain.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;

public interface HandlerObjectFileRelationsService<T> {

    void handlerRelations(Map<String, String> objectPath,
                          List<DevopsEnvFileResourceE> beforeSync,
                          List<T> ts,
                          Long envId, Long projectId, String path);

}
