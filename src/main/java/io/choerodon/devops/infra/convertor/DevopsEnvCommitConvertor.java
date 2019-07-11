package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommitE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvCommitConvertor.java
import io.choerodon.devops.infra.dto.DevopsEnvCommitDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsEnvCommitDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvCommitConvertor.java

@Component
public class DevopsEnvCommitConvertor implements ConvertorI<DevopsEnvCommitE, DevopsEnvCommitDO, Object> {

    @Override
    public DevopsEnvCommitE doToEntity(DevopsEnvCommitDO devopsEnvCommitDO) {
        DevopsEnvCommitE devopsEnvCommitE = new DevopsEnvCommitE();
        BeanUtils.copyProperties(devopsEnvCommitDO, devopsEnvCommitE);
        return devopsEnvCommitE;
    }

    @Override
    public DevopsEnvCommitDO entityToDo(DevopsEnvCommitE devopsEnvCommitE) {
        DevopsEnvCommitDO devopsEnvCommitDO = new DevopsEnvCommitDO();
        BeanUtils.copyProperties(devopsEnvCommitE, devopsEnvCommitDO);
        return devopsEnvCommitDO;
    }


}
