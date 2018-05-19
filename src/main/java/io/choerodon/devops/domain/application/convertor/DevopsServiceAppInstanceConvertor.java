package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsServiceAppInstanceE;
import io.choerodon.devops.infra.dataobject.DevopsServiceAppInstanceDO;

/**
 * Created by Zenger on 2018/4/19.
 */
@Service
public class DevopsServiceAppInstanceConvertor implements ConvertorI<DevopsServiceAppInstanceE, DevopsServiceAppInstanceDO, Object> {

    @Override
    public DevopsServiceAppInstanceE doToEntity(DevopsServiceAppInstanceDO dataObject) {
        DevopsServiceAppInstanceE devopsServiceAppInstanceE = new DevopsServiceAppInstanceE();
        BeanUtils.copyProperties(dataObject, devopsServiceAppInstanceE);
        return devopsServiceAppInstanceE;
    }

    @Override
    public DevopsServiceAppInstanceDO entityToDo(DevopsServiceAppInstanceE entity) {
        DevopsServiceAppInstanceDO devopsServiceAppInstanceDO = new DevopsServiceAppInstanceDO();
        BeanUtils.copyProperties(entity, devopsServiceAppInstanceDO);
        return devopsServiceAppInstanceDO;
    }
}
