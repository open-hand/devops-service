package io.choerodon.devops.infra.convertor;

import java.io.File;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 17:09
 * Description:
 */
@Component
public class DevopsEnvFileResourceConvertor implements ConvertorI<DevopsEnvFileResourceE, DevopsEnvFileResourceDO, Object> {

    @Override
    public DevopsEnvFileResourceE doToEntity(DevopsEnvFileResourceDO devopsEnvFileResourceDO) {
        DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
        BeanUtils.copyProperties(devopsEnvFileResourceDO, devopsEnvFileResourceE);
        DevopsEnvironmentE environmentE = new DevopsEnvironmentE();
        environmentE.setId(devopsEnvFileResourceDO.getEnvId());
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
