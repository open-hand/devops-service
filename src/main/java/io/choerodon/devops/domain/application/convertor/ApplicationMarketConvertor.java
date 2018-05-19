package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.devops.domain.application.entity.ApplicationMarketE;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketDO;

/**
 * Created by ernst on 2018/5/12.
 */
@Component
public class ApplicationMarketConvertor implements ConvertorI<ApplicationMarketE, DevopsAppMarketDO, ApplicationReleasingDTO> {

    @Override
    public DevopsAppMarketDO entityToDo(ApplicationMarketE applicationMarketE) {
        DevopsAppMarketDO devopsAppMarketDO = new DevopsAppMarketDO();
        BeanUtils.copyProperties(applicationMarketE, devopsAppMarketDO);
        if (applicationMarketE.getApplicationE() != null) {
            devopsAppMarketDO.setAppId(applicationMarketE.getApplicationE().getId());
        }
        return devopsAppMarketDO;
    }

    @Override
    public ApplicationReleasingDTO entityToDto(ApplicationMarketE entity) {
        ApplicationReleasingDTO applicationReleasingDTO = new ApplicationReleasingDTO();
        BeanUtils.copyProperties(entity, applicationReleasingDTO);
        applicationReleasingDTO.setAppId(entity.getApplicationE().getId());
        return applicationReleasingDTO;
    }

    @Override
    public ApplicationMarketE doToEntity(DevopsAppMarketDO devopsAppMarketDO) {
        ApplicationMarketE applicationMarketE = new ApplicationMarketE();
        BeanUtils.copyProperties(devopsAppMarketDO, applicationMarketE);
        applicationMarketE.initApplicationEById(devopsAppMarketDO.getAppId());
        return applicationMarketE;
    }
}
