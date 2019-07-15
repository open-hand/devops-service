package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvUserPermissionVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvUserPermissionE;
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvUserPermissionConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvUserPermissionE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvUserPermissionConvertor.java
>>>>>>> [IMP] 重构Repository
import io.choerodon.devops.infra.dataobject.DevopsEnvUserPermissionDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvUserPermissionConvertor.java

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 17:11
 * Description:
 */
@Component
public class DevopsEnvUserPermissionConvertor implements ConvertorI<DevopsEnvUserPermissionE, DevopsEnvUserPermissionDTO, DevopsEnvUserPermissionVO> {
    @Override
    public DevopsEnvUserPermissionVO doToDto(DevopsEnvUserPermissionDTO dataObject) {
        DevopsEnvUserPermissionVO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionVO();
        BeanUtils.copyProperties(dataObject, devopsEnvUserPermissionDTO);
        return devopsEnvUserPermissionDTO;
    }

    @Override
    public DevopsEnvUserPermissionDTO entityToDo(DevopsEnvUserPermissionE entity) {
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO = new DevopsEnvUserPermissionDTO();
        BeanUtils.copyProperties(entity, devopsEnvUserPermissionDO);
        return devopsEnvUserPermissionDO;
    }

    @Override
    public DevopsEnvUserPermissionE doToEntity(DevopsEnvUserPermissionDTO devopsEnvUserPermissionDO) {
        DevopsEnvUserPermissionE devopsEnvUserPermissionE = new DevopsEnvUserPermissionE();
        BeanUtils.copyProperties(devopsEnvUserPermissionDO, devopsEnvUserPermissionE);
        return devopsEnvUserPermissionE;
    }
}
