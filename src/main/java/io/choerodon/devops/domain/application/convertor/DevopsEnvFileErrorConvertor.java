package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnvFileErrorDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileErrorE;
import io.choerodon.devops.infra.dataobject.DevopsEnvFileErrorDO;

@Component
public class DevopsEnvFileErrorConvertor implements ConvertorI<DevopsEnvFileErrorE, DevopsEnvFileErrorDO, DevopsEnvFileErrorDTO> {


    @Override
    public DevopsEnvFileErrorE doToEntity(DevopsEnvFileErrorDO devopsEnvFileErrorDO) {
        DevopsEnvFileErrorE devopsEnvFileErrorE = new DevopsEnvFileErrorE();
        BeanUtils.copyProperties(devopsEnvFileErrorDO, devopsEnvFileErrorE);
        return devopsEnvFileErrorE;
    }

    @Override
    public DevopsEnvFileErrorDO entityToDo(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO();
        BeanUtils.copyProperties(devopsEnvFileErrorE, devopsEnvFileErrorDO);
        return devopsEnvFileErrorDO;
    }

    @Override
    public DevopsEnvFileErrorDTO entityToDto(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO();
        BeanUtils.copyProperties(devopsEnvFileErrorE, devopsEnvFileErrorDTO);
        return devopsEnvFileErrorDTO;
    }
}
