package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnvPodContainerDTO;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodContainerDO;

@Component
public class DevopsEnvPodContainerConvertor implements ConvertorI<Object, DevopsEnvPodContainerDO, DevopsEnvPodContainerDTO> {
    @Override
    public DevopsEnvPodContainerDTO doToDto(DevopsEnvPodContainerDO containerDO) {
        DevopsEnvPodContainerDTO containerDTO = new DevopsEnvPodContainerDTO();
        BeanUtils.copyProperties(containerDO, containerDTO);
        return containerDTO;
    }
}