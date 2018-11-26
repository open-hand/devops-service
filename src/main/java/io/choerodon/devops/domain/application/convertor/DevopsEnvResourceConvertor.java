package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsEnvResourceE;
import io.choerodon.devops.domain.application.factory.DevopsInstanceResourceFactory;
import io.choerodon.devops.infra.dataobject.DevopsEnvResourceDO;

/**
 * Created by younger on 2018/4/24.
 */
@Component
public class DevopsEnvResourceConvertor implements ConvertorI<DevopsEnvResourceE, DevopsEnvResourceDO, Object> {

    @Override
    public DevopsEnvResourceE doToEntity(DevopsEnvResourceDO devopsEnvResourceDO) {
        DevopsEnvResourceE devopsEnvResourceE = DevopsInstanceResourceFactory.createDevopsInstanceResourceE();
        devopsEnvResourceE.initApplicationInstanceE(devopsEnvResourceDO.getAppInstanceId());
        devopsEnvResourceE.initDevopsInstanceResourceMessageE(devopsEnvResourceDO.getResourceDetailId());
        devopsEnvResourceE.initDevopsEnvCommandE(devopsEnvResourceDO.getCommandId());
        devopsEnvResourceE.initDevopsEnvironmentE(devopsEnvResourceDO.getEnvId());
        BeanUtils.copyProperties(devopsEnvResourceDO, devopsEnvResourceE);
        return devopsEnvResourceE;
    }

    @Override
    public DevopsEnvResourceDO entityToDo(DevopsEnvResourceE devopsEnvResourceE) {
        DevopsEnvResourceDO devopsEnvResourceDO = new DevopsEnvResourceDO();
        BeanUtils.copyProperties(devopsEnvResourceE, devopsEnvResourceDO);
        if (devopsEnvResourceE.getApplicationInstanceE() != null) {
            devopsEnvResourceDO.setAppInstanceId(devopsEnvResourceE.getApplicationInstanceE().getId());
        }
        if (devopsEnvResourceE.getDevopsEnvCommandE() != null) {
            devopsEnvResourceDO.setCommandId(devopsEnvResourceE.getDevopsEnvCommandE().getId());
        }
        if (devopsEnvResourceE.getDevopsEnvResourceDetailE() != null) {
            devopsEnvResourceDO.setResourceDetailId(devopsEnvResourceE.getDevopsEnvResourceDetailE().getId());
        }
        if (devopsEnvResourceE.getDevopsEnvironmentE() != null) {
            devopsEnvResourceDO.setEnvId(devopsEnvResourceE.getDevopsEnvironmentE().getId());
        }
        return devopsEnvResourceDO;
    }
}
