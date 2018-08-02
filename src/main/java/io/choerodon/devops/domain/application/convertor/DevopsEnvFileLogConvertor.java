package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnvFileLogDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileLogE;
import io.choerodon.devops.infra.dataobject.DevopsEnvFileLogDO;

@Component
public class DevopsEnvFileLogConvertor implements ConvertorI<DevopsEnvFileLogE, DevopsEnvFileLogDO, DevopsEnvFileLogDTO> {


    @Override
    public DevopsEnvFileLogE doToEntity(DevopsEnvFileLogDO devopsEnvFileLogDO) {
        DevopsEnvFileLogE devopsEnvFileLogE = new DevopsEnvFileLogE();
        BeanUtils.copyProperties(devopsEnvFileLogDO, devopsEnvFileLogE);
        return devopsEnvFileLogE;
    }

    @Override
    public DevopsEnvFileLogDO entityToDo(DevopsEnvFileLogE devopsEnvFileLogE) {
        DevopsEnvFileLogDO devopsEnvFileLogDO = new DevopsEnvFileLogDO();
        BeanUtils.copyProperties(devopsEnvFileLogE, devopsEnvFileLogDO);
        return devopsEnvFileLogDO;
    }

    @Override
    public DevopsEnvFileLogDTO entityToDto(DevopsEnvFileLogE devopsEnvFileLogE) {
        DevopsEnvFileLogDTO devopsEnvFileLogDTO = new DevopsEnvFileLogDTO();
        BeanUtils.copyProperties(devopsEnvFileLogE, devopsEnvFileLogDTO);
        return devopsEnvFileLogDTO;
    }
}
