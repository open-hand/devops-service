package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1Endpoints;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceVO;

public interface HandlerObjectFileRelationsService<T> {

    void handlerRelations(Map<String, String> objectPath,
                          List<DevopsEnvFileResourceVO> beforeSync,
                          List<T> ts,
                          List<V1Endpoints> v1Endpoints,
                          Long envId, Long projectId, String path, Long userId);

}
