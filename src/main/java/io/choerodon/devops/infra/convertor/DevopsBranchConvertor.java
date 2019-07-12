package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsBranchVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsBranchE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsBranchConvertor.java

import io.choerodon.devops.infra.dto.DevopsBranchDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsBranchConvertor.java
=======
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
>>>>>>> [REF] refactor DevopsBranchRepository

@Component
public class DevopsBranchConvertor implements ConvertorI<DevopsBranchE, DevopsBranchDTO, DevopsBranchVO> {


    @Override
    public DevopsBranchE doToEntity(DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchE devopsBranchE = new DevopsBranchE();
        BeanUtils.copyProperties(devopsBranchDTO, devopsBranchE);
        if (devopsBranchDTO.getAppId() != null) {
            devopsBranchE.initApplicationE(devopsBranchDTO.getAppId());
        }
        return devopsBranchE;
    }

    @Override
    public DevopsBranchDTO entityToDo(DevopsBranchE devopsBranchE) {
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        BeanUtils.copyProperties(devopsBranchE, devopsBranchDTO);
        if (devopsBranchE.getApplicationE() != null) {
            devopsBranchDTO.setAppId(devopsBranchE.getApplicationE().getId());
        }
        return devopsBranchDTO;
    }

    @Override
    public DevopsBranchE dtoToEntity(DevopsBranchVO devopsBranchVO) {
        DevopsBranchE devopsBranchE = new DevopsBranchE();
        BeanUtils.copyProperties(devopsBranchVO, devopsBranchE);
        return devopsBranchE;
    }

    @Override
    public DevopsBranchVO entityToDto(DevopsBranchE devopsBranchE) {
        DevopsBranchVO devopsBranchVO = new DevopsBranchVO();
        BeanUtils.copyProperties(devopsBranchE, devopsBranchVO);
        return devopsBranchVO;
    }

    @Override
    public DevopsBranchVO doToDto(DevopsBranchDTO dataObject) {
        DevopsBranchVO devopsBranchVO = new DevopsBranchVO();
        BeanUtils.copyProperties(dataObject, devopsBranchVO);
        return devopsBranchVO;
    }
}
