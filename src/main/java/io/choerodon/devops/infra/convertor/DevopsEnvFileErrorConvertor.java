package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileErrorE;
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvFileErrorConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileErrorE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvFileErrorConvertor.java
>>>>>>> [IMP] 修改repository重构
import io.choerodon.devops.infra.dataobject.DevopsEnvFileErrorDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvFileErrorConvertor.java

@Component
public class DevopsEnvFileErrorConvertor implements ConvertorI<DevopsEnvFileErrorE, DevopsEnvFileErrorDTO, DevopsEnvFileErrorVO> {

    @Override
    public DevopsEnvFileErrorE doToEntity(DevopsEnvFileErrorDTO devopsEnvFileErrorDO) {
        DevopsEnvFileErrorE devopsEnvFileErrorE = new DevopsEnvFileErrorE();
        BeanUtils.copyProperties(devopsEnvFileErrorDO, devopsEnvFileErrorE);
        devopsEnvFileErrorE.setErrorTime(devopsEnvFileErrorDO.getLastUpdateDate());
        return devopsEnvFileErrorE;
    }

    @Override
    public DevopsEnvFileErrorDTO entityToDo(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDO = new DevopsEnvFileErrorDTO();
        BeanUtils.copyProperties(devopsEnvFileErrorE, devopsEnvFileErrorDO);
        if (devopsEnvFileErrorE.getErrorTime() != null) {
            devopsEnvFileErrorDO.setLastUpdateDate(devopsEnvFileErrorE.getErrorTime());
        }
        return devopsEnvFileErrorDO;
    }

    @Override
    public DevopsEnvFileErrorVO entityToDto(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorVO devopsEnvFileErrorDTO = new DevopsEnvFileErrorVO();
        BeanUtils.copyProperties(devopsEnvFileErrorE, devopsEnvFileErrorDTO);
        return devopsEnvFileErrorDTO;
    }
}
