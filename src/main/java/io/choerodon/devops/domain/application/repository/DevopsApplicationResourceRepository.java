package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.DevopsAppResourceE;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
public interface DevopsApplicationResourceRepository {

    void baseCreate(DevopsAppResourceE devopsAppResourceE);

    void baseDeleteByAppIdAndType(Long appId, String type);

    void baseDeleteByResourceIdAndType(Long resourceId, String type);

    List<DevopsAppResourceE> baseQueryByApplicationAndType(Long appId, String type);
}
