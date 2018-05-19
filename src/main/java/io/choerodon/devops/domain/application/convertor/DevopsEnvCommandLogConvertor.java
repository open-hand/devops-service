package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandLogE;
import io.choerodon.devops.domain.application.factory.DevopsInstanceResourceLogFactory;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommandLogDO;

/**
 * Created by younger on 2018/4/24.
 */
@Component
public class DevopsEnvCommandLogConvertor implements ConvertorI<DevopsEnvCommandLogE, DevopsEnvCommandLogDO, Object> {

    @Override
    public DevopsEnvCommandLogE doToEntity(DevopsEnvCommandLogDO devopsEnvCommandLogDO) {
        DevopsEnvCommandLogE devopsEnvCommandLogE =
                DevopsInstanceResourceLogFactory.createDevopsInstanceResourceLogE();
        BeanUtils.copyProperties(devopsEnvCommandLogDO, devopsEnvCommandLogE);
        if (devopsEnvCommandLogDO.getCommandId() != null) {
            devopsEnvCommandLogE.initDevopsEnvCommandE(devopsEnvCommandLogDO.getCommandId());
        }
        return devopsEnvCommandLogE;
    }

    @Override
    public DevopsEnvCommandLogDO entityToDo(DevopsEnvCommandLogE devopsEnvCommandLogE) {
        DevopsEnvCommandLogDO devopsEnvCommandLogDO = new DevopsEnvCommandLogDO();
        BeanUtils.copyProperties(devopsEnvCommandLogE, devopsEnvCommandLogDO);
        if (devopsEnvCommandLogE.getDevopsEnvCommandE() != null) {
            devopsEnvCommandLogDO.setCommandId(devopsEnvCommandLogE.getDevopsEnvCommandE().getId());
        }
        return devopsEnvCommandLogDO;
    }

}
