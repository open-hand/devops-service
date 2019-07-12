package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandValueVO;
import io.choerodon.devops.domain.application.factory.DevopsEnvCommandValueFactory;
import io.choerodon.devops.infra.dto.DevopsEnvCommandValueDTO;

@Component
public class DevopsEnvCommandValueConvertor implements ConvertorI<DevopsEnvCommandValueVO, DevopsEnvCommandValueDTO, Object> {

    @Override
    public DevopsEnvCommandValueVO doToEntity(DevopsEnvCommandValueDTO devopsEnvCommandValueDO) {
        DevopsEnvCommandValueVO devopsEnvCommandValueE = DevopsEnvCommandValueFactory.createDevopsEnvCommandE();
        BeanUtils.copyProperties(devopsEnvCommandValueDO, devopsEnvCommandValueE);
        return devopsEnvCommandValueE;
    }

    @Override
    public DevopsEnvCommandValueDTO entityToDo(DevopsEnvCommandValueVO devopsEnvCommandValueE) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDO = new DevopsEnvCommandValueDTO();
        BeanUtils.copyProperties(devopsEnvCommandValueE, devopsEnvCommandValueDO);
        return devopsEnvCommandValueDO;
    }

}
