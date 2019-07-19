package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvironmentUpdateVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.factory.DevopsEnvironmentFactory;

@Component
public class DevopsEnviromentUpdateConvertor implements ConvertorI<DevopsEnvironmentE, Object, DevopsEnvironmentUpdateVO> {

    @Override
    public DevopsEnvironmentUpdateVO entityToDto(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO = new DevopsEnvironmentUpdateVO();
        if (devopsEnvironmentE.getClusterE() != null) {
            devopsEnvironmentUpdateDTO.setClusterId(devopsEnvironmentE.getClusterE().getId());
        }
        BeanUtils.copyProperties(devopsEnvironmentE, devopsEnvironmentUpdateDTO);
        return devopsEnvironmentUpdateDTO;
    }


    @Override
    public DevopsEnvironmentE dtoToEntity(DevopsEnvironmentUpdateVO devopsEnvironmentUpdateDTO) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        BeanUtils.copyProperties(devopsEnvironmentUpdateDTO, devopsEnvironmentE);
        if (devopsEnvironmentUpdateDTO.getClusterId() != null) {
            devopsEnvironmentE.initDevopsClusterEById(devopsEnvironmentUpdateDTO.getClusterId());
        }
        return devopsEnvironmentE;
    }
}