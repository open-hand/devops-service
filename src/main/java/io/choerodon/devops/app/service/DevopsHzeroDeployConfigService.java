package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployConfigDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 10:01
 */
public interface DevopsHzeroDeployConfigService {

    DevopsHzeroDeployConfigDTO baseSave(DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO);

    DevopsHzeroDeployConfigDTO baseQueryById(Long valueId);

    void update(DevopsHzeroDeployConfigDTO devopsHzeroDeployConfigDTO);

    void updateById(Long id, String value, DevopsServiceReqVO devopsServiceReqVO, DevopsIngressVO devopsIngressVO);
}
