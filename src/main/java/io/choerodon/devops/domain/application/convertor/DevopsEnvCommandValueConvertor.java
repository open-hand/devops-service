package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandValueE;
import io.choerodon.devops.domain.application.factory.DevopsEnvCommandValueFactory;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommandValueDO;

@Component
public class DevopsEnvCommandValueConvertor implements ConvertorI<DevopsEnvCommandValueE, DevopsEnvCommandValueDO, Object> {

    @Override
    public DevopsEnvCommandValueE doToEntity(DevopsEnvCommandValueDO devopsEnvCommandValueDO) {
        DevopsEnvCommandValueE devopsEnvCommandValueE = DevopsEnvCommandValueFactory.createDevopsEnvCommandE();
        BeanUtils.copyProperties(devopsEnvCommandValueDO, devopsEnvCommandValueE);
        return devopsEnvCommandValueE;
    }

    @Override
    public DevopsEnvCommandValueDO entityToDo(DevopsEnvCommandValueE devopsEnvCommandValueE) {
        DevopsEnvCommandValueDO devopsEnvCommandValueDO = new DevopsEnvCommandValueDO();
        BeanUtils.copyProperties(devopsEnvCommandValueE, devopsEnvCommandValueDO);
        return devopsEnvCommandValueDO;
    }

}
