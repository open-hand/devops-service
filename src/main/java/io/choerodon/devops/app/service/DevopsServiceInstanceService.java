package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsServiceInstanceDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsServiceInstanceService {

    void baseCreate(DevopsServiceInstanceDTO devopsServiceInstanceDTO);

    DevopsServiceInstanceDTO baseQueryByOptions(Long serviceId, Long instanceId);

    List<DevopsServiceInstanceDTO> baseListByServiceId(Long serviceId);

    void baseDeleteByOptions(Long serviceId, String instanceCode);

    void baseUpdateInstanceId(Long serviceInstanceId, Long instanceId);

    void baseDeleteById(Long id);

    List<DevopsServiceInstanceDTO> baseListByInstanceId(Long instanceId);

    List<DevopsServiceInstanceDTO> baseListByEnvIdAndInstanceCode(Long envId, String instanceCode);
}
