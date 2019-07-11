package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.DevopsAppResourceE;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
public interface DevopsAppResourceRepository {

    void insert(DevopsAppResourceE devopsAppResourceE);

    void deleteByAppIdAndType(Long appId, String type);

    void deleteByResourceIdAndType(Long resourceId, String type);

    List<DevopsAppResourceE> queryByAppAndType(Long appId, String type);
}
