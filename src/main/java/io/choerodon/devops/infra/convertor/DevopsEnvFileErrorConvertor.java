package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileErrorE;
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


@Component
public class DevopsEnvFileErrorConvertor implements ConvertorI<DevopsEnvFileErrorE, DevopsEnvFileErrorDO, DevopsEnvFileErrorDTO> {

    @Override
    public DevopsEnvFileErrorE doToEntity(DevopsEnvFileErrorDO devopsEnvFileErrorDO) {
        DevopsEnvFileErrorE devopsEnvFileErrorE = new DevopsEnvFileErrorE();
        BeanUtils.copyProperties(devopsEnvFileErrorDO, devopsEnvFileErrorE);
        devopsEnvFileErrorE.setErrorTime(devopsEnvFileErrorDO.getLastUpdateDate());
        return devopsEnvFileErrorE;
    }

    @Override
    public DevopsEnvFileErrorDO entityToDo(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO();
        BeanUtils.copyProperties(devopsEnvFileErrorE, devopsEnvFileErrorDO);
        if (devopsEnvFileErrorE.getErrorTime() != null) {
            devopsEnvFileErrorDO.setLastUpdateDate(devopsEnvFileErrorE.getErrorTime());
        }
        return devopsEnvFileErrorDO;
    }

    @Override
    public DevopsEnvFileErrorDTO entityToDto(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO();
        BeanUtils.copyProperties(devopsEnvFileErrorE, devopsEnvFileErrorDTO);
        return devopsEnvFileErrorDTO;
    }
}
