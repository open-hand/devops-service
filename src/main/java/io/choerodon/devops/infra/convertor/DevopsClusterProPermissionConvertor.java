package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterProPermissionE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsClusterProPermissionConvertor.java
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsClusterProPermissionDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsClusterProPermissionConvertor.java

@Component
public class DevopsClusterProPermissionConvertor implements ConvertorI<DevopsClusterProPermissionE, DevopsClusterProPermissionDO, Object> {


    @Override
    public DevopsClusterProPermissionE doToEntity(DevopsClusterProPermissionDO devopsClusterProPermissionDO) {
        DevopsClusterProPermissionE devopsClusterProPermissionE = new DevopsClusterProPermissionE();
        BeanUtils.copyProperties(devopsClusterProPermissionDO, devopsClusterProPermissionE);
        return devopsClusterProPermissionE;
    }

    @Override
    public DevopsClusterProPermissionDO entityToDo(DevopsClusterProPermissionE devopsClusterProPermissionE) {
        DevopsClusterProPermissionDO devopsClusterProPermissionDO = new DevopsClusterProPermissionDO();
        BeanUtils.copyProperties(devopsClusterProPermissionE, devopsClusterProPermissionDO);
        return devopsClusterProPermissionDO;
    }
}
