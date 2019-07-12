package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsCommandEventE;

public interface DevopsCommandEventRepository {

    void baseCreate(DevopsCommandEventE devopsCommandEventE);

    List<DevopsCommandEventE> baseListByCommandIdAndType(Long commandId, String type);

    void baseDeletePreInstanceCommandEvent(Long instanceId);

    void baseDeleteByCommandId(Long commandId);
}
