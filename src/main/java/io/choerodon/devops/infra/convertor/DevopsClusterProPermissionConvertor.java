package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterProPermissionE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsClusterProPermissionConvertor.java
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsClusterProPermissionDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsClusterProPermissionConvertor.java
=======
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;
>>>>>>> [REF] refactor DevopsClusterProPermissionRepository

@Component
public class DevopsClusterProPermissionConvertor implements ConvertorI<DevopsClusterProPermissionE, DevopsClusterProPermissionDTO, Object> {


    @Override
    public DevopsClusterProPermissionE doToEntity(DevopsClusterProPermissionDTO devopsClusterProPermissionDTO) {
        DevopsClusterProPermissionE devopsClusterProPermissionE = new DevopsClusterProPermissionE();
        BeanUtils.copyProperties(devopsClusterProPermissionDTO, devopsClusterProPermissionE);
        return devopsClusterProPermissionE;
    }

    @Override
    public DevopsClusterProPermissionDTO entityToDo(DevopsClusterProPermissionE devopsClusterProPermissionE) {
        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
        BeanUtils.copyProperties(devopsClusterProPermissionE, devopsClusterProPermissionDTO);
        return devopsClusterProPermissionDTO;
    }
}
