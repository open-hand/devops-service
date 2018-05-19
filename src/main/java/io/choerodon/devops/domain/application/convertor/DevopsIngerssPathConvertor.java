package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsIngressPathDTO;
import io.choerodon.devops.domain.application.entity.DevopsIngressPathE;
import io.choerodon.devops.infra.dataobject.DevopsIngressPathDO;

/**
 * Created by younger on 2018/4/28.
 */
@Component
public class DevopsIngerssPathConvertor implements ConvertorI<DevopsIngressPathE, DevopsIngressPathDO, DevopsIngressPathDTO> {

    @Override
    public DevopsIngressPathE dtoToEntity(DevopsIngressPathDTO dto) {
        DevopsIngressPathE devopsIngressPathE = new DevopsIngressPathE();
        BeanUtils.copyProperties(dto, devopsIngressPathE);
        return devopsIngressPathE;
    }

    @Override
    public DevopsIngressPathDTO entityToDto(DevopsIngressPathE entity) {
        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        BeanUtils.copyProperties(entity, devopsIngressPathDTO);
        return devopsIngressPathDTO;
    }

    @Override
    public DevopsIngressPathE doToEntity(DevopsIngressPathDO dataObject) {
        DevopsIngressPathE devopsIngressPathE = new DevopsIngressPathE();
        BeanUtils.copyProperties(dataObject, devopsIngressPathE);
        devopsIngressPathE.initDevopsIngressE(dataObject.getIngressId());
        return devopsIngressPathE;
    }

    @Override
    public DevopsIngressPathDO entityToDo(DevopsIngressPathE entity) {
        DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO();
        BeanUtils.copyProperties(entity, devopsIngressPathDO);
        devopsIngressPathDO.setIngressId(entity.getDevopsIngressE().getId());
        return devopsIngressPathDO;
    }
}
