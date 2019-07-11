package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.factory.DevopsEnvCommandFactory;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommandDO;

@Component
public class DevopsEnvCommandConvertor implements ConvertorI<DevopsEnvCommandE, DevopsEnvCommandDO, Object> {

    @Override
    public DevopsEnvCommandE doToEntity(DevopsEnvCommandDO devopsEnvCommandDO) {
        DevopsEnvCommandE devopsEnvCommandE = DevopsEnvCommandFactory.createDevopsEnvCommandE();
        BeanUtils.copyProperties(devopsEnvCommandDO, devopsEnvCommandE);
        if (devopsEnvCommandDO.getValueId() != null) {
            devopsEnvCommandE.initDevopsEnvCommandValueE(devopsEnvCommandDO.getValueId());
        }
        return devopsEnvCommandE;
    }

    @Override
    public DevopsEnvCommandDO entityToDo(DevopsEnvCommandE devopsEnvCommandE) {
        DevopsEnvCommandDO devopsEnvCommandDO = new DevopsEnvCommandDO();
        BeanUtils.copyProperties(devopsEnvCommandE, devopsEnvCommandDO);
        if (devopsEnvCommandE.getDevopsEnvCommandValueE() != null) {
            devopsEnvCommandDO.setValueId(devopsEnvCommandE.getDevopsEnvCommandValueE().getId());
        }
        return devopsEnvCommandDO;
    }
}
