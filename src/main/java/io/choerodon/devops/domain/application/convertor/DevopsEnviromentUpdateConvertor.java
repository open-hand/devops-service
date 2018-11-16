package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnvironmentUpdateDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.factory.DevopsEnvironmentFactory;

@Component
public class DevopsEnviromentUpdateConvertor implements ConvertorI<DevopsEnvironmentE, Object, DevopsEnvironmentUpdateDTO> {

    @Override
    public DevopsEnvironmentUpdateDTO entityToDto(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO = new DevopsEnvironmentUpdateDTO();
        BeanUtils.copyProperties(devopsEnvironmentE, devopsEnvironmentUpdateDTO);
        return devopsEnvironmentUpdateDTO;
    }


    @Override
    public DevopsEnvironmentE dtoToEntity(DevopsEnvironmentUpdateDTO devopsEnvironmentUpdateDTO) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        BeanUtils.copyProperties(devopsEnvironmentUpdateDTO, devopsEnvironmentE);
        if (devopsEnvironmentUpdateDTO.getClusterId() != null) {
            devopsEnvironmentE.initDevopsClusterEById(devopsEnvironmentUpdateDTO.getClusterId());
        }
        return devopsEnvironmentE;
    }
}