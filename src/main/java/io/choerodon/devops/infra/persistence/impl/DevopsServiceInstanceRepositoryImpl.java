package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsServiceAppInstanceE;
import io.choerodon.devops.domain.application.repository.DevopsServiceInstanceRepository;
import io.choerodon.devops.infra.dataobject.DevopsServiceAppInstanceDO;
import io.choerodon.devops.infra.mapper.DevopsServiceAppInstanceMapper;

/**
 * Created by Zenger on 2018/4/19.
 */
@Component
public class DevopsServiceInstanceRepositoryImpl implements DevopsServiceInstanceRepository {

    private DevopsServiceAppInstanceMapper devopsServiceAppInstanceMapper;

    public DevopsServiceInstanceRepositoryImpl(DevopsServiceAppInstanceMapper devopsServiceAppInstanceMapper) {
        this.devopsServiceAppInstanceMapper = devopsServiceAppInstanceMapper;
    }

    @Override
    public void insert(DevopsServiceAppInstanceE devopsServiceAppInstanceE) {
        if (devopsServiceAppInstanceMapper.insert(
                ConvertHelper.convert(devopsServiceAppInstanceE, DevopsServiceAppInstanceDO.class)) != 1) {
            throw new CommonException("error.service.app.instance.insert");
        }
    }

    @Override
    public DevopsServiceAppInstanceE queryByOptions(Long serviceId, Long instanceId) {
        DevopsServiceAppInstanceDO devopsServiceAppInstanceDO = new DevopsServiceAppInstanceDO();
        devopsServiceAppInstanceDO.setServiceId(serviceId);
        devopsServiceAppInstanceDO.setAppInstanceId(instanceId);
        return ConvertHelper.convert(devopsServiceAppInstanceMapper
                .selectOne(devopsServiceAppInstanceDO), DevopsServiceAppInstanceE.class);
    }

    @Override
    public List<DevopsServiceAppInstanceE> selectByServiceId(Long serviceId) {
        DevopsServiceAppInstanceDO devopsServiceAppInstanceDO = new DevopsServiceAppInstanceDO();
        devopsServiceAppInstanceDO.setServiceId(serviceId);
        return ConvertHelper.convertList(devopsServiceAppInstanceMapper
                .select(devopsServiceAppInstanceDO), DevopsServiceAppInstanceE.class);
    }

    @Override
    public void deleteByOptions(Long serviceId, Long instanceId) {
        DevopsServiceAppInstanceDO devopsServiceAppInstanceDO = new DevopsServiceAppInstanceDO();
        devopsServiceAppInstanceDO.setServiceId(serviceId);
        devopsServiceAppInstanceDO.setAppInstanceId(instanceId);
        devopsServiceAppInstanceMapper.delete(devopsServiceAppInstanceDO);
    }

    @Override
    public void deleteById(Long id) {
        devopsServiceAppInstanceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<DevopsServiceAppInstanceE> queryByServiceId(Long serviceId) {
        DevopsServiceAppInstanceDO devopsServiceAppInstanceDO = new DevopsServiceAppInstanceDO();
        devopsServiceAppInstanceDO.setServiceId(serviceId);
        return ConvertHelper.convertList(devopsServiceAppInstanceMapper.select(devopsServiceAppInstanceDO),DevopsServiceAppInstanceE.class);
    }
}
