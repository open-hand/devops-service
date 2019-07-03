package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsAppResourceDTO;
import io.choerodon.devops.domain.application.entity.DevopsAppResourceE;
import io.choerodon.devops.infra.dataobject.DevopsAppResourceDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
@Component
public class DevopsAppResourceConvertor implements ConvertorI<DevopsAppResourceE, DevopsAppResourceDO, DevopsAppResourceDTO> {

    @Override
    public DevopsAppResourceE doToEntity(DevopsAppResourceDO resourceDO) {
        DevopsAppResourceE resourceE = new DevopsAppResourceE();
        BeanUtils.copyProperties(resourceDO, resourceE);
        return resourceE;
    }

    @Override
    public DevopsAppResourceDO entityToDo(DevopsAppResourceE resourceE) {
        DevopsAppResourceDO resourceDO = new DevopsAppResourceDO();
        BeanUtils.copyProperties(resourceE, resourceDO);
        return resourceDO;
    }

    @Override
    public DevopsAppResourceE dtoToEntity(DevopsAppResourceDTO resourceDTO) {
        DevopsAppResourceE resourceE = new DevopsAppResourceE();
        BeanUtils.copyProperties(resourceDTO, resourceE);
        return resourceE;
    }

    @Override
    public DevopsAppResourceDTO entityToDto(DevopsAppResourceE entity) {
        DevopsAppResourceDTO resourceDTO = new DevopsAppResourceDTO();
        BeanUtils.copyProperties(entity, resourceDTO);
        return resourceDTO;
    }
}
