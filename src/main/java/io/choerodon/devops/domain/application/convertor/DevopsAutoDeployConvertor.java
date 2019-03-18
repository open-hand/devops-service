package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsAutoDeployDTO;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployE;
import io.choerodon.devops.infra.dataobject.DevopsAutoDeployDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:27 2019/2/26
 * Description:
 */
@Component
public class DevopsAutoDeployConvertor implements ConvertorI<DevopsAutoDeployE, DevopsAutoDeployDO, DevopsAutoDeployDTO> {

    @Override
    public DevopsAutoDeployE dtoToEntity(DevopsAutoDeployDTO devopsAutoDeployDTO) {
        DevopsAutoDeployE devopsAutoDeployE = new DevopsAutoDeployE();
        BeanUtils.copyProperties(devopsAutoDeployDTO, devopsAutoDeployE);
        devopsAutoDeployE.setTriggerVersion(devopsAutoDeployDTO.getTriggerVersion().stream().collect(Collectors.joining(",")));
        return devopsAutoDeployE;
    }

    @Override
    public DevopsAutoDeployDO entityToDo(DevopsAutoDeployE devopsAutoDeployE) {
        DevopsAutoDeployDO devopsAutoDeployDO = new DevopsAutoDeployDO();
        BeanUtils.copyProperties(devopsAutoDeployE, devopsAutoDeployDO);
        return devopsAutoDeployDO;
    }

    @Override
    public DevopsAutoDeployDTO entityToDto(DevopsAutoDeployE devopsAutoDeployE) {
        DevopsAutoDeployDTO devopsAutoDeployDTO = new DevopsAutoDeployDTO();
        BeanUtils.copyProperties(devopsAutoDeployE, devopsAutoDeployDTO);
        if (devopsAutoDeployE.getTriggerVersion() != null && !devopsAutoDeployE.getTriggerVersion().isEmpty()) {
            devopsAutoDeployDTO.setTriggerVersion(Arrays.asList(devopsAutoDeployE.getTriggerVersion().split(",")));
        } else {
            devopsAutoDeployDTO.setTriggerVersion(new ArrayList<>());
        }
        return devopsAutoDeployDTO;
    }

    @Override
    public DevopsAutoDeployE doToEntity(DevopsAutoDeployDO devopsAutoDeployDO) {
        DevopsAutoDeployE devopsAutoDeployE = new DevopsAutoDeployE();
        BeanUtils.copyProperties(devopsAutoDeployDO, devopsAutoDeployE);
        return devopsAutoDeployE;
    }
}
