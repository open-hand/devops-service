package io.choerodon.devops.domain.application.repository;

import java.util.List;

<<<<<<< HEAD
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
=======
>>>>>>> [IMP] 重构Repository

/**
 * Creator: Runge
 * Date: 2018/4/17
 * Time: 13:52
 * Description:
 */
public interface DeployDetailRepository {
<<<<<<< HEAD
    List<DevopsEnvPodDTO> baseGetPods(Long instanceId);
=======
    List<DevopsEnvPodDTO> getPods(Long instanceId);
>>>>>>> [IMP] 重构Repository
}
