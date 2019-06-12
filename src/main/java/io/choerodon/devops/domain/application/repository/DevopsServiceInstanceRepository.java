package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsServiceAppInstanceE;

/**
 * Created by Zenger on 2018/4/19.
 */
public interface DevopsServiceInstanceRepository {

    void insert(DevopsServiceAppInstanceE devopsServiceInstanceE);

    DevopsServiceAppInstanceE queryByOptions(Long serviceId, Long instanceId);

    List<DevopsServiceAppInstanceE> selectByServiceId(Long serviceId);

    void deleteByOptions(Long serviceId, String instanceCode);

    void deleteById(Long id);

    List<DevopsServiceAppInstanceE> selectByInstanceId(Long instanceId);
}
