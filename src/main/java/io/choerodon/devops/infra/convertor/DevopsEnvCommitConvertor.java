package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommitE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvCommitConvertor.java
import io.choerodon.devops.infra.dto.DevopsEnvCommitDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsEnvCommitDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvCommitConvertor.java
=======
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommitVO;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvCommitConvertor.java
import io.choerodon.devops.infra.dataobject.DevopsEnvCommitDO;
=======
import io.choerodon.devops.infra.dto.DevopsEnvCommitDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvCommitConvertor.java
>>>>>>> [IMP] 修改repository重构

@Component
public class DevopsEnvCommitConvertor implements ConvertorI<DevopsEnvCommitVO, DevopsEnvCommitDTO, Object> {

    @Override
    public DevopsEnvCommitVO doToEntity(DevopsEnvCommitDTO devopsEnvCommitDO) {
        DevopsEnvCommitVO devopsEnvCommitE = new DevopsEnvCommitVO();
        BeanUtils.copyProperties(devopsEnvCommitDO, devopsEnvCommitE);
        return devopsEnvCommitE;
    }

    @Override
    public DevopsEnvCommitDTO entityToDo(DevopsEnvCommitVO devopsEnvCommitE) {
        DevopsEnvCommitDTO devopsEnvCommitDO = new DevopsEnvCommitDTO();
        BeanUtils.copyProperties(devopsEnvCommitE, devopsEnvCommitDO);
        return devopsEnvCommitDO;
    }


}
