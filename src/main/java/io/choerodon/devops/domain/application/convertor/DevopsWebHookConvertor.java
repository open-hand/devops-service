package io.choerodon.devops.domain.application.convertor;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsAppWebHookE;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsAppWebHookDO;

@Component
public class DevopsWebHookConvertor implements ConvertorI<DevopsAppWebHookE, DevopsAppWebHookDO, Object> {

    @Override
    public DevopsAppWebHookDO entityToDo(DevopsAppWebHookE devopsAppWebHookE) {
        DevopsAppWebHookDO devopsAppWebHookDO = new DevopsAppWebHookDO();
        if (devopsAppWebHookE.getApplicationE() != null) {
            devopsAppWebHookDO.setAppId(devopsAppWebHookE.getApplicationE().getId());
        }
        if (devopsAppWebHookE.getProjectHook() != null) {
            devopsAppWebHookDO.setHookId(TypeUtil.objToLong(devopsAppWebHookE.getProjectHook().getId()));
        }
        return devopsAppWebHookDO;
    }

    @Override
    public DevopsAppWebHookE doToEntity(DevopsAppWebHookDO devopsAppWebHookDO) {
        DevopsAppWebHookE devopsAppWebHookE = new DevopsAppWebHookE();
        if (devopsAppWebHookDO.getAppId() != null) {
            devopsAppWebHookE.initApplicationE(devopsAppWebHookDO.getAppId());
        }
        if (devopsAppWebHookDO.getHookId() != null) {
            devopsAppWebHookE.initProjectHook(TypeUtil.objToInteger(devopsAppWebHookDO.getHookId()));
        }
        return devopsAppWebHookE;
    }


}
