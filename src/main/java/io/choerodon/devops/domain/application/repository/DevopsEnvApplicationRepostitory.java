package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.DevopsEnvMessageVO;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/1
 */

public interface DevopsEnvApplicationRepostitory {
    DevopsEnvApplicationE baseCreate(DevopsEnvApplicationE devopsEnvApplicationE);

    List<Long> baseListAppByEnvId(Long envId);

    List<DevopsEnvMessageVO> baseListResourceByEnvAndApp(Long envId, Long appId);
}
