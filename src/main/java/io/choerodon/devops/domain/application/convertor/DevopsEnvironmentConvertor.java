package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnviromentDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.factory.DevopsEnvironmentFactory;
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO;

/**
 * Created by younger on 2018/4/9.
 */
@Component
public class DevopsEnvironmentConvertor implements ConvertorI<DevopsEnvironmentE, DevopsEnvironmentDO, DevopsEnviromentDTO> {

    @Override
    public DevopsEnvironmentE doToEntity(DevopsEnvironmentDO devopsEnvironmentDO) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        BeanUtils.copyProperties(devopsEnvironmentDO, devopsEnvironmentE);
        devopsEnvironmentE.initProjectE(devopsEnvironmentDO.getProjectId());
        return devopsEnvironmentE;
    }

    @Override
    public DevopsEnvironmentDO entityToDo(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO();
        if(devopsEnvironmentE.getProjectE()!=null) {
            devopsEnvironmentDO.setProjectId(devopsEnvironmentE.getProjectE().getId());
        }
        BeanUtils.copyProperties(devopsEnvironmentE, devopsEnvironmentDO);
        return devopsEnvironmentDO;
    }


    @Override
    public DevopsEnvironmentE dtoToEntity(DevopsEnviromentDTO devopsEnviromentDTO) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        devopsEnvironmentE.initProjectE(devopsEnviromentDTO.getProjectId());
        BeanUtils.copyProperties(devopsEnviromentDTO, devopsEnvironmentE);
        return devopsEnvironmentE;
    }
}
