package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvUserPermissionE;
import io.choerodon.devops.infra.dataobject.DevopsEnvUserPermissionDO;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 17:11
 * Description:
 */
@Component
public class DevopsEnvUserPermissionConvertor implements ConvertorI<DevopsEnvUserPermissionE, DevopsEnvUserPermissionDO, DevopsEnvUserPermissionDTO> {
    @Override
    public DevopsEnvUserPermissionDTO doToDto(DevopsEnvUserPermissionDO dataObject) {
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO();
        BeanUtils.copyProperties(dataObject, devopsEnvUserPermissionDTO);
        devopsEnvUserPermissionDTO.setPermitted(dataObject.getPermitted());
        return devopsEnvUserPermissionDTO;
    }

    @Override
    public DevopsEnvUserPermissionDO entityToDo(DevopsEnvUserPermissionE entity) {
        DevopsEnvUserPermissionDO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDO();
        BeanUtils.copyProperties(entity, devopsEnvUserPermissionDO);
        return devopsEnvUserPermissionDO;
    }

    @Override
    public DevopsEnvUserPermissionE doToEntity(DevopsEnvUserPermissionDO devopsEnvUserPermissionDO) {
        DevopsEnvUserPermissionE devopsEnvUserPermissionE = new DevopsEnvUserPermissionE();
        BeanUtils.copyProperties(devopsEnvUserPermissionDO, devopsEnvUserPermissionE);
        return devopsEnvUserPermissionE;
    }
}
