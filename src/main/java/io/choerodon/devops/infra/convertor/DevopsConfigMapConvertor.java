package io.choerodon.devops.infra.convertor;


import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsConfigMapVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsConfigMapE;
import io.choerodon.devops.infra.dto.DevopsConfigMapDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsConfigMapConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsConfigMapDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsConfigMapE;
import io.choerodon.devops.infra.dataobject.DevopsConfigMapDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsConfigMapConvertor.java
=======
>>>>>>> [REF] refactor DevopsBranchRepository

@Component
public class DevopsConfigMapConvertor implements ConvertorI<DevopsConfigMapE, DevopsConfigMapDTO, DevopsConfigMapVO> {
    @Override
    public DevopsConfigMapE doToEntity(DevopsConfigMapDTO devopsConfigMapDTO) {
        DevopsConfigMapE devopsConfigMapE = new DevopsConfigMapE();
        BeanUtils.copyProperties(devopsConfigMapDTO, devopsConfigMapE);
        if (devopsConfigMapDTO.getEnvId() != null) {
            devopsConfigMapE.initDevopsEnvironmentE(devopsConfigMapDTO.getEnvId());
        }
        if (devopsConfigMapDTO.getCommandId() != null) {
            devopsConfigMapE.initDevopsEnvCommandE(devopsConfigMapDTO.getCommandId());
        }
        return devopsConfigMapE;
    }

    @Override
    public DevopsConfigMapDTO entityToDo(DevopsConfigMapE devopsConfigMapE) {
        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
        BeanUtils.copyProperties(devopsConfigMapE, devopsConfigMapDTO);
        if (devopsConfigMapE.getDevopsEnvCommandE() != null) {
            devopsConfigMapDTO.setCommandId(devopsConfigMapE.getDevopsEnvCommandE().getId());
        }
        if (devopsConfigMapE.getDevopsEnvironmentE() != null) {
            devopsConfigMapDTO.setEnvId(devopsConfigMapE.getDevopsEnvironmentE().getId());
        }
        return devopsConfigMapDTO;
    }

    @Override
    public DevopsConfigMapE dtoToEntity(DevopsConfigMapVO devopsConfigMapVO) {
        DevopsConfigMapE devopsConfigMapE = new DevopsConfigMapE();
        BeanUtils.copyProperties(devopsConfigMapVO, devopsConfigMapE);
        if (devopsConfigMapVO.getEnvId() != null) {
            devopsConfigMapE.initDevopsEnvironmentE(devopsConfigMapVO.getEnvId());
        }
        return devopsConfigMapE;
    }


}
