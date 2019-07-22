package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsServiceAppInstanceDTO;

/**
 * Created by Zenger on 2018/4/19.
 */
public interface DevopsServiceInstanceRepository {

    void baseCreate(DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO);

    DevopsServiceAppInstanceDTO baseQueryByOptions(Long serviceId, Long instanceId);

    List<DevopsServiceAppInstanceE> baseListByServiceId(Long serviceId);

    void baseDeleteByOptions(Long serviceId, String instanceCode);

    void baseUpdateInstanceId(Long serviceInstanceId, Long instanceId);

    void baseDeleteById(Long id);

    List<DevopsServiceAppInstanceDTO> baseListByInstanceId(Long instanceId);

    List<DevopsServiceAppInstanceDTO> baseListByEnvIdAndInstanceCode(Long envId, String instanceCode);
}
