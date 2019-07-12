package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.DevopsAppResourceE;

/**
 * @author zmf
 */
public interface DevopsApplicationResourceService {
    void baseCreate(DevopsAppResourceE devopsAppResourceE);

    void baseDeleteByAppIdAndType(Long appId, String type);

    void baseDeleteByResourceIdAndType(Long resourceId, String type);

    List<DevopsAppResourceE> baseQueryByApplicationAndType(Long appId, String type);
}
