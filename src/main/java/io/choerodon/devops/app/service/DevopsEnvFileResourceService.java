package io.choerodon.devops.app.service;

import java.util.Map;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceE;

public interface DevopsEnvFileResourceService {

    void updateOrCreateFileResource(Map<String, String> objectPath,
                                    Long envId,
                                    DevopsEnvFileResourceE devopsEnvFileResourceE,
                                    Integer i, Long id, String kind);
}
