package io.choerodon.devops.domain.application.convertor;

import java.io.File;

import org.springframework.beans.BeanUtils;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.infra.dataobject.DevopsEnvFileResourceDO;

/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 17:09
 * Description:
 */
public class DevopsEnvFileResourceConvertor implements ConvertorI<DevopsEnvFileResourceE, DevopsEnvFileResourceDO, Object> {

    @Override
    public DevopsEnvFileResourceE doToEntity(DevopsEnvFileResourceDO devopsEnvFileResourceDO) {
        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
        BeanUtils.copyProperties(devopsEnvFileResourceDO, devopsEnvFileResourceE);
        DevopsEnvironmentE environmentE = new DevopsEnvironmentE();
        environmentE.setId(devopsEnvFileResourceDO.getId());
        devopsEnvFileResourceE.setEnvironment(environmentE);
        devopsEnvFileResourceE.setFile(new File(devopsEnvFileResourceDO.getFilePath()));
        return devopsEnvFileResourceE;
    }

    @Override
    public DevopsEnvFileResourceDO entityToDo(DevopsEnvFileResourceE devopsEnvFileResourceE) {
        DevopsEnvFileResourceDO devopsEnvFileResourceDO = new DevopsEnvFileResourceDO();
        BeanUtils.copyProperties(devopsEnvFileResourceE, devopsEnvFileResourceDO);
        if (devopsEnvFileResourceE.getEnvironment() != null) {
            devopsEnvFileResourceDO.setEnvId(devopsEnvFileResourceE.getEnvironment().getId());
        }
        return devopsEnvFileResourceDO;
    }
}
