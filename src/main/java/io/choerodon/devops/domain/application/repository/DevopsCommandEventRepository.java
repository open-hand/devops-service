package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsCommandEventE;

public interface DevopsCommandEventRepository {

    void create(DevopsCommandEventE devopsCommandEventE);

    List<DevopsCommandEventE> listByCommandIdAndType(Long commandId, String type);

    void deletePreInstanceCommandEvent(Long instanceId);

}
