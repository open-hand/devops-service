package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsCommandEventE;
import io.choerodon.devops.infra.dto.DevopsCommandEventDO;


@Component
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
