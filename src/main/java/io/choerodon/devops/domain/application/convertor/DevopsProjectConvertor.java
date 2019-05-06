package io.choerodon.devops.domain.application.convertor;

import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;

/**
 * Created by younger on 2018/4/2.
 */
@Component
public class DevopsProjectConvertor implements ConvertorI<DevopsProjectE, DevopsProjectDO, Object> {

    @Override
    public DevopsProjectE doToEntity(DevopsProjectDO devopsProjectDO) {
        DevopsProjectE devopsProjectE = new DevopsProjectE();
        BeanUtils.copyProperties(devopsProjectDO,devopsProjectE);
        devopsProjectE.initProjectE(devopsProjectDO.getIamProjectId());
        return devopsProjectE;
    }

    @Override
    public DevopsProjectDO entityToDo(DevopsProjectE devopsProjectE) {
        DevopsProjectDO devopsProjectDO = new DevopsProjectDO();
        BeanUtils.copyProperties(devopsProjectE,devopsProjectDO);
        devopsProjectDO.setIamProjectId(devopsProjectE.getProjectE().getId());
        return devopsProjectDO;
    }
}
