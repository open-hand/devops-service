package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvFileDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileE;
import io.choerodon.devops.infra.dto.DevopsEnvFileDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


@Component
public class DevopsEnvFileConvertor implements ConvertorI<DevopsEnvFileE, DevopsEnvFileDO, DevopsEnvFileDTO> {


    @Override
    public DevopsEnvFileE doToEntity(DevopsEnvFileDO devopsEnvFileDO) {
        DevopsEnvFileE devopsEnvFileE = new DevopsEnvFileE();
        BeanUtils.copyProperties(devopsEnvFileDO, devopsEnvFileE);
        return devopsEnvFileE;
    }

    @Override
    public DevopsEnvFileDO entityToDo(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileDO devopsEnvFileDO = new DevopsEnvFileDO();
        BeanUtils.copyProperties(devopsEnvFileE, devopsEnvFileDO);
        return devopsEnvFileDO;
    }

    @Override
    public DevopsEnvFileDTO entityToDto(DevopsEnvFileE devopsEnvFileE) {
        DevopsEnvFileDTO devopsEnvFileDTO = new DevopsEnvFileDTO();
        BeanUtils.copyProperties(devopsEnvFileE, devopsEnvFileDTO);
        return devopsEnvFileDTO;
    }
}
