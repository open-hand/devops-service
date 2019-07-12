package io.choerodon.devops.infra.convertor;

import io.choerodon.devops.infra.dto.DevopsCommandEventDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsCommandEventE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsCommandEventConvertor.java
import io.choerodon.devops.infra.dto.DevopsCommandEventDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsCommandEventDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsCommandEventConvertor.java
=======
>>>>>>> [REF] refactor DevopsCommandEventRepository


@Component
public class DevopsCommandEventConvertor implements ConvertorI<DevopsCommandEventE, DevopsCommandEventDTO, Object> {

    @Override
    public DevopsCommandEventE doToEntity(DevopsCommandEventDTO devopsCommandEventDTO) {
        DevopsCommandEventE devopsCommandEventE = new DevopsCommandEventE();
        BeanUtils.copyProperties(devopsCommandEventDTO, devopsCommandEventE);
        if (devopsCommandEventDTO.getCommandId() != null) {
            devopsCommandEventE.initDevopsEnvCommandE(devopsCommandEventDTO.getCommandId());
        }
        return devopsCommandEventE;
    }

    @Override
    public DevopsCommandEventDTO entityToDo(DevopsCommandEventE devopsCommandEventE) {
        DevopsCommandEventDTO devopsCommandEventDTO = new DevopsCommandEventDTO();
        BeanUtils.copyProperties(devopsCommandEventE, devopsCommandEventDTO);
        if (devopsCommandEventE.getDevopsEnvCommandE() != null) {
            devopsCommandEventDTO.setCommandId(devopsCommandEventE.getDevopsEnvCommandE().getId());
        }
        return devopsCommandEventDTO;
    }
}
