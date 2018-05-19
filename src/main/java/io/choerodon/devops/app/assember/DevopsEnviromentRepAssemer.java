package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnviromentRepDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;

/**
 * Created by younger on 2018/4/9.
 */
@Component
public class DevopsEnviromentRepAssemer implements ConvertorI<DevopsEnvironmentE, Object, DevopsEnviromentRepDTO> {

    @Override
    public DevopsEnviromentRepDTO entityToDto(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnviromentRepDTO devopsEnviromentRepDTO = new DevopsEnviromentRepDTO();
        BeanUtils.copyProperties(devopsEnvironmentE, devopsEnviromentRepDTO);
        return devopsEnviromentRepDTO;
    }

}
