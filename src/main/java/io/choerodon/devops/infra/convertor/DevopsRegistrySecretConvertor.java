package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsRegistrySecretE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsRegistrySecretConvertor.java
import io.choerodon.devops.infra.dto.DevopsRegistrySecretDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsRegistrySecretDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsRegistrySecretConvertor.java
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Created by Sheep on 2019/3/14.
 */
@Component
public class DevopsRegistrySecretConvertor implements ConvertorI<DevopsRegistrySecretE, DevopsRegistrySecretDO, Object> {

    @Override
    public DevopsRegistrySecretE doToEntity(DevopsRegistrySecretDO devopsRegistrySecretDO) {
        DevopsRegistrySecretE devopsRegistrySecretE = new DevopsRegistrySecretE();
        BeanUtils.copyProperties(devopsRegistrySecretDO, devopsRegistrySecretE);
        if (devopsRegistrySecretDO.getConfigId() != null) {
            devopsRegistrySecretE.initDevopsProjectConfigE(devopsRegistrySecretDO.getConfigId());
        }
        if (devopsRegistrySecretDO.getEnvId() != null) {
            devopsRegistrySecretE.initDevopsEnvironmentE(devopsRegistrySecretDO.getEnvId(), devopsRegistrySecretDO.getNamespace());
        }
        return devopsRegistrySecretE;
    }

    @Override
    public DevopsRegistrySecretDO entityToDo(DevopsRegistrySecretE devopsRegistrySecretE) {
        DevopsRegistrySecretDO devopsRegistrySecretDO = new DevopsRegistrySecretDO();
        BeanUtils.copyProperties(devopsRegistrySecretE, devopsRegistrySecretDO);
        if (devopsRegistrySecretE.getDevopsEnvironmentE() != null) {
            devopsRegistrySecretDO.setEnvId(devopsRegistrySecretE.getDevopsEnvironmentE().getId());
            devopsRegistrySecretDO.setNamespace(devopsRegistrySecretE.getDevopsEnvironmentE().getCode());
        }
        if (devopsRegistrySecretE.getDevopsProjectConfigE() != null) {
            devopsRegistrySecretDO.setConfigId(devopsRegistrySecretE.getDevopsProjectConfigE().getId());
        }
        return devopsRegistrySecretDO;
    }


}
