package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.DevopsServiceAppInstanceDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsServiceInstanceService {

    void baseCreate(DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO);

    DevopsServiceAppInstanceDTO baseQueryByOptions(Long serviceId, Long instanceId);

    List<DevopsServiceAppInstanceDTO> baseListByServiceId(Long serviceId);

    void baseDeleteByOptions(Long serviceId, String instanceCode);

    void baseUpdateInstanceId(Long serviceInstanceId, Long instanceId);

    void baseDeleteById(Long id);

    List<DevopsServiceAppInstanceDTO> baseListByInstanceId(Long instanceId);

    List<DevopsServiceAppInstanceDTO> baseListByEnvIdAndInstanceCode(Long envId, String instanceCode);
}
