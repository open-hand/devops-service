package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.domain.application.entity.DevopsBranchE;
import io.choerodon.devops.infra.dataobject.DevopsBranchDO;

@Component
public class DevopsBranchConvertor implements ConvertorI<DevopsBranchE, DevopsBranchDO, DevopsBranchDTO> {


    @Override
    public DevopsBranchE doToEntity(DevopsBranchDO devopsBranchDO) {
        DevopsBranchE devopsBranchE = new DevopsBranchE();
        BeanUtils.copyProperties(devopsBranchDO, devopsBranchE);
        if (devopsBranchDO.getAppId() != null) {
            devopsBranchE.initApplicationE(devopsBranchDO.getAppId());
        }
        return devopsBranchE;
    }

    @Override
    public DevopsBranchDO entityToDo(DevopsBranchE devopsBranchE) {
        DevopsBranchDO devopsBranchDO = new DevopsBranchDO();
        BeanUtils.copyProperties(devopsBranchE, devopsBranchDO);
        if (devopsBranchE.getApplicationE() != null) {
            devopsBranchDO.setAppId(devopsBranchE.getApplicationE().getId());
        }
        return devopsBranchDO;
    }

    @Override
    public DevopsBranchE dtoToEntity(DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchE devopsBranchE = new DevopsBranchE();
        BeanUtils.copyProperties(devopsBranchDTO, devopsBranchE);
        return devopsBranchE;
    }

    @Override
    public DevopsBranchDTO entityToDto(DevopsBranchE devopsBranchE) {
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        BeanUtils.copyProperties(devopsBranchE, devopsBranchDTO);
        return devopsBranchDTO;
    }

    @Override
    public DevopsBranchDTO doToDto(DevopsBranchDO dataObject) {
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        BeanUtils.copyProperties(dataObject, devopsBranchDTO);
        return devopsBranchDTO;
    }
}
