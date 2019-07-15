package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsRegistrySecretE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsRegistrySecretConvertor.java
import io.choerodon.devops.infra.dto.DevopsRegistrySecretDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsRegistrySecretDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsRegistrySecretConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsRegistrySecretConvertor.java
import io.choerodon.devops.infra.dataobject.DevopsRegistrySecretDO;
=======
import io.choerodon.devops.infra.dto.DevopsRegistrySecretDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/DevopsRegistrySecretConvertor.java
>>>>>>> [IMP]重构后端断码
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Created by Sheep on 2019/3/14.
 */
@Component
public class DevopsRegistrySecretConvertor implements ConvertorI<DevopsRegistrySecretE, DevopsRegistrySecretDTO, Object> {

    @Override
    public DevopsRegistrySecretE doToEntity(DevopsRegistrySecretDTO devopsRegistrySecretDTO) {
        DevopsRegistrySecretE devopsRegistrySecretE = new DevopsRegistrySecretE();
        BeanUtils.copyProperties(devopsRegistrySecretDTO, devopsRegistrySecretE);
        if (devopsRegistrySecretDTO.getConfigId() != null) {
            devopsRegistrySecretE.initDevopsProjectConfigE(devopsRegistrySecretDTO.getConfigId());
        }
        if (devopsRegistrySecretDTO.getEnvId() != null) {
            devopsRegistrySecretE.initDevopsEnvironmentE(devopsRegistrySecretDTO.getEnvId(), devopsRegistrySecretDTO.getNamespace());
        }
        return devopsRegistrySecretE;
    }

    @Override
    public DevopsRegistrySecretDTO entityToDo(DevopsRegistrySecretE devopsRegistrySecretE) {
        DevopsRegistrySecretDTO devopsRegistrySecretDTO = new DevopsRegistrySecretDTO();
        BeanUtils.copyProperties(devopsRegistrySecretE, devopsRegistrySecretDTO);
        if (devopsRegistrySecretE.getDevopsEnvironmentE() != null) {
            devopsRegistrySecretDTO.setEnvId(devopsRegistrySecretE.getDevopsEnvironmentE().getId());
            devopsRegistrySecretDTO.setNamespace(devopsRegistrySecretE.getDevopsEnvironmentE().getCode());
        }
        if (devopsRegistrySecretE.getDevopsProjectConfigE() != null) {
            devopsRegistrySecretDTO.setConfigId(devopsRegistrySecretE.getDevopsProjectConfigE().getId());
        }
        return devopsRegistrySecretDTO;
    }


}
