package io.choerodon.devops.app.service;

<<<<<<< HEAD
=======
import io.choerodon.devops.api.vo.ApplicationRepVO;
import io.choerodon.devops.api.vo.DevopsEnvApplicationDTO;
import io.choerodon.devops.api.vo.DevopsEnvLabelDTO;
import io.choerodon.devops.api.vo.DevopsEnvPortDTO;

>>>>>>> [IMP] applicationController重构
import java.util.List;

import io.choerodon.devops.api.vo.DevopsEnvApplicationCreationVO;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public interface DevopsEnvApplicationService {
    List<DevopsEnvApplicationDTO> batchCreate(DevopsEnvApplicationCreationVO devopsEnvApplicationCreationVO);

    List<ApplicationRepVO> queryAppByEnvId(Long envId);

    List<DevopsEnvLabelDTO> queryLabelByAppEnvId(Long envId, Long appId);

    List<DevopsEnvPortDTO> queryPortByAppEnvId(Long envId, Long appId);
}
