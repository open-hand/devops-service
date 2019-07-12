package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvFileVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileE;
import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvFileConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvFileDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvFileConvertor.java
>>>>>>> [IMP] 修改repository重构
import io.choerodon.devops.infra.dataobject.DevopsEnvFileDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvFileConvertor.java

@Component
public class DevopsEnvFileConvertor implements ConvertorI<DevopsEnvFileE, DevopsEnvFileDTO, DevopsEnvFileVO> {


    @Override
    public DevopsEnvFileE doToEntity(DevopsEnvFileDTO devopsEnvFileDO) {
        DevopsEnvFileE devopsEnvFileE = new DevopsEnvFileE();
        BeanUtils.copyProperties(devopsEnvFileDO, devopsEnvFileE);
        return devopsEnvFileE;
    }

    @Override
    public DevopsEnvFileDTO entityToDo(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileDTO devopsEnvFileDO = new DevopsEnvFileDTO();
        BeanUtils.copyProperties(devopsEnvFileE, devopsEnvFileDO);
        return devopsEnvFileDO;
    }

    @Override
    public DevopsEnvFileVO entityToDto(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileVO devopsEnvFileDTO = new DevopsEnvFileVO();
        BeanUtils.copyProperties(devopsEnvFileE, devopsEnvFileDTO);
        return devopsEnvFileDTO;
    }
}
