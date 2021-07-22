package io.choerodon.devops.app.service;

public interface DevopsHostAppInstanceRelService {

    void saveHostAppInstanceRel(Long projectId, Long hostId, Long appServiceId, String appSource, Long instanceId, String instanceType);

    void deleteByHostIdAndInstanceInfo(Long hostId, Long id, String value);
}
