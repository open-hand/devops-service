package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.DevopsEnvApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsEnvMessageE;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/1
 */

public interface DevopsEnvApplicationRepostitory {
    DevopsEnvApplicationE create(DevopsEnvApplicationE devopsEnvApplicationE);

    List<Long> queryAppByEnvId(Long envId);

    List<DevopsEnvMessageE> listResourceByEnvAndApp(Long envId,Long appId);
}
