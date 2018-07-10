package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsCommandEventE;
import io.choerodon.devops.infra.dataobject.DevopsCommandEventDO;


@Service
public class DevopsCommandEventConvertor implements ConvertorI<DevopsCommandEventE, DevopsCommandEventDO, Object> {

    @Override
    public DevopsCommandEventE doToEntity(DevopsCommandEventDO devopsCommandEventDO) {
        DevopsCommandEventE devopsCommandEventE = new DevopsCommandEventE();
        BeanUtils.copyProperties(devopsCommandEventDO, devopsCommandEventE);
        if (devopsCommandEventDO.getCommandId() != null) {
            devopsCommandEventE.initDevopsEnvCommandE(devopsCommandEventDO.getCommandId());
        }
        return devopsCommandEventE;
    }

    @Override
    public DevopsCommandEventDO entityToDo(DevopsCommandEventE devopsCommandEventE) {
        DevopsCommandEventDO devopsCommandEventDO = new DevopsCommandEventDO();
        BeanUtils.copyProperties(devopsCommandEventE, devopsCommandEventDO);
        if (devopsCommandEventE.getDevopsEnvCommandE() != null) {
            devopsCommandEventDO.setCommandId(devopsCommandEventE.getDevopsEnvCommandE().getId());
        }
        return devopsCommandEventDO;
    }
}
