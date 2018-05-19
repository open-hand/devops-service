package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnviromentDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.factory.DevopsEnvironmentFactory;

/**
 * Created by younger on 2018/4/9.
 */
@Component
public class DevopsEnvironmentAssember implements ConvertorI<DevopsEnvironmentE, Object, DevopsEnviromentDTO> {


    @Override
    public DevopsEnvironmentE dtoToEntity(DevopsEnviromentDTO devopsEnviromentDTO) {
        DevopsEnvironmentE devopsEnvironmentE = DevopsEnvironmentFactory.createDevopsEnvironmentE();
        devopsEnvironmentE.initProjectE(devopsEnviromentDTO.getProjectId());
        BeanUtils.copyProperties(devopsEnviromentDTO, devopsEnvironmentE);
        return devopsEnvironmentE;
    }

}
