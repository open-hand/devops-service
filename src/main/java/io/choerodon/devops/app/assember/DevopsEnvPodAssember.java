package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodDO;

/**
 * Creator: Runge
 * Date: 2018/4/17
 * Time: 16:11
 * Description:
 */
@Component
public class DevopsEnvPodAssember implements ConvertorI<Object, DevopsEnvPodDO, DevopsEnvPodDTO> {
    @Override
    public DevopsEnvPodDTO doToDto(DevopsEnvPodDO devopsEnvPodDO) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        BeanUtils.copyProperties(devopsEnvPodDO, devopsEnvPodDTO);
        devopsEnvPodDTO.setCreationDate(devopsEnvPodDO.getCreationDate());
        return devopsEnvPodDTO;
    }
}
