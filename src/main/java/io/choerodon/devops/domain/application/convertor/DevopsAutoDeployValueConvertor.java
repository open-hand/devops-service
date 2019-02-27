package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployValueE;
import io.choerodon.devops.infra.dataobject.DevopsAutoDeployValueDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:20 2019/2/26
 * Description:
 */
@Component
public class DevopsAutoDeployValueConvertor implements ConvertorI<DevopsAutoDeployValueE, DevopsAutoDeployValueDO, Object> {
    @Override
    public DevopsAutoDeployValueDO entityToDo(DevopsAutoDeployValueE devopsAutoDeployValueE) {
        DevopsAutoDeployValueDO devopsAutoDeployValueDO = new DevopsAutoDeployValueDO();
        BeanUtils.copyProperties(devopsAutoDeployValueE, devopsAutoDeployValueDO);
        return devopsAutoDeployValueDO;
    }

    @Override
    public DevopsAutoDeployValueE doToEntity(DevopsAutoDeployValueDO devopsAutoDeployValueDO) {
        DevopsAutoDeployValueE devopsAutoDeployValueE = new DevopsAutoDeployValueE();
        BeanUtils.copyProperties(devopsAutoDeployValueDO, devopsAutoDeployValueE);
        return devopsAutoDeployValueE;
    }
}
