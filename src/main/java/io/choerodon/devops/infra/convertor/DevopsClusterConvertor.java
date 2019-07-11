package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsClusterReqDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsClusterConvertor.java
import io.choerodon.devops.infra.dto.DevopsClusterDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsClusterDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsClusterConvertor.java


@Component
public class DevopsClusterConvertor implements ConvertorI<DevopsClusterE, DevopsClusterDO, DevopsClusterReqDTO> {

    @Override
    public DevopsClusterE doToEntity(DevopsClusterDO devopsClusterDO) {
        DevopsClusterE devopsClusterE = new DevopsClusterE();
        BeanUtils.copyProperties(devopsClusterDO, devopsClusterE);
        return devopsClusterE;
    }

    @Override
    public DevopsClusterDO entityToDo(DevopsClusterE devopsClusterE) {
        DevopsClusterDO devopsClusterDO = new DevopsClusterDO();
        BeanUtils.copyProperties(devopsClusterE, devopsClusterDO);
        return devopsClusterDO;
    }

    @Override
    public DevopsClusterE dtoToEntity(DevopsClusterReqDTO devopsClusterReqDTO) {
        DevopsClusterE devopsClusterE = new DevopsClusterE();
        BeanUtils.copyProperties(devopsClusterReqDTO, devopsClusterE);
        return devopsClusterE;
    }

}
