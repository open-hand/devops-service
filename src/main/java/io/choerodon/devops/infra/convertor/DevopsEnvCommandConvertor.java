package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandVO;
import io.choerodon.devops.domain.application.factory.DevopsEnvCommandFactory;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;

@Component
public class DevopsEnvCommandConvertor implements ConvertorI<DevopsEnvCommandVO, DevopsEnvCommandDTO, Object> {

    @Override
    public DevopsEnvCommandVO doToEntity(DevopsEnvCommandDTO devopsEnvCommandDO) {
        DevopsEnvCommandVO devopsEnvCommandE = DevopsEnvCommandFactory.createDevopsEnvCommandE();
        BeanUtils.copyProperties(devopsEnvCommandDO, devopsEnvCommandE);
        if (devopsEnvCommandDO.getValueId() != null) {
            devopsEnvCommandE.initDevopsEnvCommandValueE(devopsEnvCommandDO.getValueId());
        }
        return devopsEnvCommandE;
    }

    @Override
    public DevopsEnvCommandDTO entityToDo(DevopsEnvCommandVO devopsEnvCommandE) {
        DevopsEnvCommandDTO devopsEnvCommandDO = new DevopsEnvCommandDTO();
        BeanUtils.copyProperties(devopsEnvCommandE, devopsEnvCommandDO);
        if (devopsEnvCommandE.getDevopsEnvCommandValueDTO() != null) {
            devopsEnvCommandDO.setValueId(devopsEnvCommandE.getDevopsEnvCommandValueDTO().getId());
        }
        return devopsEnvCommandDO;
    }
}
