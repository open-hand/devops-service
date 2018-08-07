package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommitE;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommitDO;

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
