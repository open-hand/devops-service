package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsServiceInstanceService;
import io.choerodon.devops.infra.dto.DevopsServiceInstanceDTO;
import io.choerodon.devops.infra.mapper.DevopsServiceInstanceMapper;

/**
 * Created by Sheep on 2019/7/15.
 */

@Service
public class DevopsServiceInstanceServiceImpl implements DevopsServiceInstanceService {


    @Autowired
    private DevopsServiceInstanceMapper devopsServiceInstanceMapper;

    @Override
    public void baseCreate(DevopsServiceInstanceDTO devopsServiceInstanceDTO) {
        if (devopsServiceInstanceMapper.insert(
                devopsServiceInstanceDTO) != 1) {
            throw new CommonException("error.service.app.instance.insert");
        }
    }

    @Override
    public DevopsServiceInstanceDTO baseQueryByOptions(Long serviceId, Long instanceId) {
        DevopsServiceInstanceDTO devopsServiceInstanceDTO = new DevopsServiceInstanceDTO();
        devopsServiceInstanceDTO.setServiceId(serviceId);
        devopsServiceInstanceDTO.setInstanceId(instanceId);
        return devopsServiceInstanceMapper
                .selectOne(devopsServiceInstanceDTO);
    }

    @Override
    public List<DevopsServiceInstanceDTO> baseListByServiceId(Long serviceId) {
        DevopsServiceInstanceDTO devopsServiceInstanceDTO = new DevopsServiceInstanceDTO();
        devopsServiceInstanceDTO.setServiceId(serviceId);
        return devopsServiceInstanceMapper
                .select(devopsServiceInstanceDTO);
    }

    @Override
    public void baseDeleteByOptions(Long serviceId, String instanceCode) {
        DevopsServiceInstanceDTO devopsServiceInstanceDTO = new DevopsServiceInstanceDTO();
        devopsServiceInstanceDTO.setServiceId(serviceId);
        devopsServiceInstanceDTO.setCode(instanceCode);
        devopsServiceInstanceMapper.delete(devopsServiceInstanceDTO);
    }

    @Override
    public void baseUpdateInstanceId(Long serviceInstanceId, Long instanceId) {
        DevopsServiceInstanceDTO devopsServiceInstanceDTO = devopsServiceInstanceMapper.selectByPrimaryKey(serviceInstanceId);
        devopsServiceInstanceDTO.setInstanceId(instanceId);
        devopsServiceInstanceMapper.updateByPrimaryKeySelective(devopsServiceInstanceDTO);
    }

    @Override
    public void baseDeleteById(Long id) {
        devopsServiceInstanceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<DevopsServiceInstanceDTO> baseListByInstanceId(Long instanceId) {
        DevopsServiceInstanceDTO devopsServiceInstanceDTO = new DevopsServiceInstanceDTO();
        devopsServiceInstanceDTO.setInstanceId(instanceId);
        return devopsServiceInstanceMapper
                .select(devopsServiceInstanceDTO);
    }

    @Override
    public List<DevopsServiceInstanceDTO> baseListByEnvIdAndInstanceCode(Long envId, String instanceCode) {
        return ConvertHelper.convertList(devopsServiceInstanceMapper.listByEnvIdAndInstanceCode(instanceCode, envId), DevopsServiceInstanceDTO.class);
    }
}
