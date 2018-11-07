package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsClusterProPermissionE;
import io.choerodon.devops.infra.dataobject.DevopsClusterProPermissionDO;

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
