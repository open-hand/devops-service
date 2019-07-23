package io.choerodon.devops.domain.application.repository;

import java.util.List;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvCommandLogRepository {

    DevopsEnvCommandLogVO baseCreate(DevopsEnvCommandLogVO devopsEnvCommandLogE);

    DevopsEnvCommandLogVO baseQuery(Long logId);

    List<DevopsEnvCommandLogVO> baseListByDeployId(Long deployId);

    void baseDeleteByInstanceId(Long instanceId);

    void baseDeleteByCommandId(Long commandId);
}
