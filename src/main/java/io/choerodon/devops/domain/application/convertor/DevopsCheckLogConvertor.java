package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsCheckLogE;
import io.choerodon.devops.infra.dataobject.DevopsCheckLogDO;

@Component
public class DevopsCheckLogConvertor implements ConvertorI<DevopsCheckLogE, DevopsCheckLogDO, Object> {


    @Override
    public DevopsCheckLogDO entityToDo(DevopsCheckLogE devopsCheckLogE) {
        DevopsCheckLogDO devopsCheckLogDO = new DevopsCheckLogDO();
        BeanUtils.copyProperties(devopsCheckLogE, devopsCheckLogDO);
        return devopsCheckLogDO;
    }

    @Override
    public DevopsCheckLogE doToEntity(DevopsCheckLogDO devopsCheckLogDO) {
        DevopsCheckLogE devopsCheckLogE = new DevopsCheckLogE();
        BeanUtils.copyProperties(devopsCheckLogDO, devopsCheckLogE);
        return devopsCheckLogE;
    }

}
