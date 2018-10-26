package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.EnvUserPermissionDTO;
import io.choerodon.devops.infra.dataobject.DevopsEnvUserPermissionDO;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 17:11
 * Description:
 */
@Component
public class EnvUserPermissionConvertor implements ConvertorI<Object, DevopsEnvUserPermissionDO, EnvUserPermissionDTO> {
    @Override
    public EnvUserPermissionDTO doToDto(DevopsEnvUserPermissionDO dataObject) {
        EnvUserPermissionDTO envUserPermissionDTO = new EnvUserPermissionDTO();
        BeanUtils.copyProperties(dataObject, envUserPermissionDTO);
        envUserPermissionDTO.setDeployment(dataObject.getPermission());
        return envUserPermissionDTO;
    }
}
