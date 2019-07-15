package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsServiceInstanceService;
import io.choerodon.devops.infra.dto.DevopsServiceAppInstanceDTO;
import io.choerodon.devops.infra.mapper.DevopsServiceAppInstanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/15.
 */

@Service
public class DevopsServiceInstanceServiceImpl implements DevopsServiceInstanceService {


    @Autowired
    private DevopsServiceAppInstanceMapper devopsServiceAppInstanceMapper;


    public void baseCreate(DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO) {
        if (devopsServiceAppInstanceMapper.insert(
                devopsServiceAppInstanceDTO) != 1) {
            throw new CommonException("error.service.app.instance.insert");
        }
    }

    public DevopsServiceAppInstanceDTO baseQueryByOptions(Long serviceId, Long instanceId) {
        DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO = new DevopsServiceAppInstanceDTO();
        devopsServiceAppInstanceDTO.setServiceId(serviceId);
        devopsServiceAppInstanceDTO.setAppInstanceId(instanceId);
        return devopsServiceAppInstanceMapper
                .selectOne(devopsServiceAppInstanceDTO);
    }

    public List<DevopsServiceAppInstanceDTO> baseListByServiceId(Long serviceId) {
        DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO = new DevopsServiceAppInstanceDTO();
        devopsServiceAppInstanceDTO.setServiceId(serviceId);
        return devopsServiceAppInstanceMapper
                .select(devopsServiceAppInstanceDTO);
    }

    public void baseDeleteByOptions(Long serviceId, String instanceCode) {
        DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO = new DevopsServiceAppInstanceDTO();
        devopsServiceAppInstanceDTO.setServiceId(serviceId);
        devopsServiceAppInstanceDTO.setCode(instanceCode);
        devopsServiceAppInstanceMapper.delete(devopsServiceAppInstanceDTO);
    }

    public void baseUpdateInstanceId(Long serviceInstanceId, Long instanceId) {
        DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO = devopsServiceAppInstanceMapper.selectByPrimaryKey(serviceInstanceId);
        devopsServiceAppInstanceDTO.setAppInstanceId(instanceId);
        devopsServiceAppInstanceMapper.updateByPrimaryKeySelective(devopsServiceAppInstanceDTO);
    }

    public void baseDeleteById(Long id) {
        devopsServiceAppInstanceMapper.deleteByPrimaryKey(id);
    }

    public List<DevopsServiceAppInstanceDTO> baseListByInstanceId(Long instanceId) {
        DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO = new DevopsServiceAppInstanceDTO();
        devopsServiceAppInstanceDTO.setAppInstanceId(instanceId);
        return devopsServiceAppInstanceMapper
                .select(devopsServiceAppInstanceDTO);
    }

    public List<DevopsServiceAppInstanceDTO> baseListByEnvIdAndInstanceCode(Long envId, String instanceCode) {
        return ConvertHelper.convertList(devopsServiceAppInstanceMapper.listByEnvIdAndInstanceCode(instanceCode, envId), DevopsServiceAppInstanceDTO.class);
    }






}
