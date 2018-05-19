package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnvPodContainerDTO;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodContainerDO;

/**
 * Creator: Runge
 * Date: 2018/5/17
 * Time: 10:14
 * Description:
 */
@Component
public class DevopsEnvPodContainerAssember implements ConvertorI<Object, DevopsEnvPodContainerDO, DevopsEnvPodContainerDTO> {
    @Override
    public DevopsEnvPodContainerDTO doToDto(DevopsEnvPodContainerDO containerDO) {
        DevopsEnvPodContainerDTO containerDTO = new DevopsEnvPodContainerDTO();
        BeanUtils.copyProperties(containerDO, containerDTO);
        return containerDTO;
    }
}
