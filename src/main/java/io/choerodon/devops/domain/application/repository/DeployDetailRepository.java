package io.choerodon.devops.domain.application.repository;

import java.util.List;

<<<<<<< HEAD
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
<<<<<<< HEAD
=======
>>>>>>> [IMP] 重构Repository
=======
>>>>>>> [IMP]修改后端代码结构

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
