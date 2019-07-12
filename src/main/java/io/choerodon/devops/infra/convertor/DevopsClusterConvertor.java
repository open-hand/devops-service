package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsClusterReqDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsClusterConvertor.java
import io.choerodon.devops.infra.dto.DevopsClusterDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsClusterDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsClusterConvertor.java
=======
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
>>>>>>> [REF] refactor DevopsClusterRepository


@Component
public class DevopsClusterConvertor implements ConvertorI<DevopsClusterE, DevopsClusterDTO, DevopsClusterReqDTO> {

    @Override
    public DevopsClusterE doToEntity(DevopsClusterDTO devopsClusterDTO) {
        DevopsClusterE devopsClusterE = new DevopsClusterE();
        BeanUtils.copyProperties(devopsClusterDTO, devopsClusterE);
        return devopsClusterE;
    }

    @Override
    public DevopsClusterDTO entityToDo(DevopsClusterE devopsClusterE) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        BeanUtils.copyProperties(devopsClusterE, devopsClusterDTO);
        return devopsClusterDTO;
    }

    @Override
    public DevopsClusterE dtoToEntity(DevopsClusterReqDTO devopsClusterReqDTO) {
        DevopsClusterE devopsClusterE = new DevopsClusterE();
        BeanUtils.copyProperties(devopsClusterReqDTO, devopsClusterE);
        return devopsClusterE;
    }

}
