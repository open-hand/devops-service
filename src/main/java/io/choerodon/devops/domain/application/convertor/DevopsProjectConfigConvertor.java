package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;
import io.choerodon.devops.infra.dataobject.DevopsProjectConfigDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@Component
public class DevopsProjectConfigConvertor implements ConvertorI<DevopsProjectConfigE, DevopsProjectConfigDO, DevopsProjectConfigDTO> {

    @Override
    public DevopsProjectConfigE doToEntity(DevopsProjectConfigDO devopsProjectConfigDO) {
        DevopsProjectConfigE testAppInstanceE = new DevopsProjectConfigE();
        BeanUtils.copyProperties(devopsProjectConfigDO, testAppInstanceE);
        return testAppInstanceE;
    }

    @Override
    public DevopsProjectConfigDO entityToDo(DevopsProjectConfigE devopsProjectConfigE) {
        DevopsProjectConfigDO devopsProjectConfigDO = new DevopsProjectConfigDO();
        BeanUtils.copyProperties(devopsProjectConfigE, devopsProjectConfigDO);
        return devopsProjectConfigDO;
    }


    @Override
    public DevopsProjectConfigE dtoToEntity(DevopsProjectConfigDTO devopsProjectConfigDTO) {
        DevopsProjectConfigE devopsProjectConfigE = new DevopsProjectConfigE();
        BeanUtils.copyProperties(devopsProjectConfigDTO, devopsProjectConfigE);
        return devopsProjectConfigE;
    }

    @Override
    public DevopsProjectConfigDTO entityToDto(DevopsProjectConfigE devopsProjectConfigE) {
        DevopsProjectConfigDTO devopsProjectConfigDTO = new DevopsProjectConfigDTO();
        BeanUtils.copyProperties(devopsProjectConfigE, devopsProjectConfigDTO);
        return devopsProjectConfigDTO;
    }
}
