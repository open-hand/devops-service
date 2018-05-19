package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsEnvResourceDetailE;
import io.choerodon.devops.domain.application.factory.DevopsInstanceResourceMessageFactory;
import io.choerodon.devops.infra.dataobject.DevopsEnvResourceDetailDO;

/**
 * Created by younger on 2018/4/24.
 */
@Component
public class DevopsEnvResourceDetailConvertor implements ConvertorI<DevopsEnvResourceDetailE, DevopsEnvResourceDetailDO, Object> {


    @Override
    public DevopsEnvResourceDetailE doToEntity(DevopsEnvResourceDetailDO devopsEnvResourceDetailDO) {
        DevopsEnvResourceDetailE devopsEnvResourceDetailE =
                DevopsInstanceResourceMessageFactory.createDevopsInstanceResourceMessageE();
        BeanUtils.copyProperties(devopsEnvResourceDetailDO, devopsEnvResourceDetailE);
        return devopsEnvResourceDetailE;
    }

    @Override
    public DevopsEnvResourceDetailDO entityToDo(DevopsEnvResourceDetailE devopsEnvResourceDetailE) {
        DevopsEnvResourceDetailDO devopsEnvResourceDetailDO = new DevopsEnvResourceDetailDO();
        BeanUtils.copyProperties(devopsEnvResourceDetailE, devopsEnvResourceDetailDO);
        return devopsEnvResourceDetailDO;
    }
}
