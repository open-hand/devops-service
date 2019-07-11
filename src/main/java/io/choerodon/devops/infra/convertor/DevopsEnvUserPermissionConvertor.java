package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvUserPermissionE;
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


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
