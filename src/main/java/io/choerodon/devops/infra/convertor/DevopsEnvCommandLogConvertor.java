package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.infra.dto.DevopsEnvCommandLogDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/4/24.
 */
@Component
public class DevopsEnvCommandLogConvertor implements ConvertorI<DevopsEnvCommandLogVO, DevopsEnvCommandLogDTO, Object> {

    @Override
    public DevopsEnvCommandLogVO doToEntity(DevopsEnvCommandLogDTO devopsEnvCommandLogDTO) {
        DevopsEnvCommandLogVO devopsEnvCommandLogE =
                DevopsInstanceResourceLogFactory.createDevopsInstanceResourceLogE();
        BeanUtils.copyProperties(devopsEnvCommandLogDTO, devopsEnvCommandLogE);
        if (devopsEnvCommandLogDTO.getCommandId() != null) {
            devopsEnvCommandLogE.initDevopsEnvCommandE(devopsEnvCommandLogDTO.getCommandId());
        }
        return devopsEnvCommandLogE;
    }

    @Override
    public DevopsEnvCommandLogDTO entityToDo(DevopsEnvCommandLogVO devopsEnvCommandLogE) {
        DevopsEnvCommandLogDTO devopsEnvCommandLogDTO = new DevopsEnvCommandLogDTO();
        BeanUtils.copyProperties(devopsEnvCommandLogE, devopsEnvCommandLogDTO);
        if (devopsEnvCommandLogE.getDevopsEnvCommandVO() != null) {
            devopsEnvCommandLogDTO.setCommandId(devopsEnvCommandLogE.getDevopsEnvCommandVO().getId());
        }
        return devopsEnvCommandLogDTO;
    }

}
