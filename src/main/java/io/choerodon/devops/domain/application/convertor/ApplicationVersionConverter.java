package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ApplicationVersionRepDTO;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.factory.ApplicationVersionEFactory;
import io.choerodon.devops.infra.dataobject.ApplicationVersionDO;

@Component
public class ApplicationVersionConverter implements ConvertorI<ApplicationVersionE, ApplicationVersionDO, ApplicationVersionRepDTO> {

    @Override
    public ApplicationVersionE doToEntity(ApplicationVersionDO applicationVersionDO) {
        ApplicationVersionE applicationVersionE = ApplicationVersionEFactory.create();
        BeanUtils.copyProperties(applicationVersionDO, applicationVersionE);
        applicationVersionE.initApplicationE(applicationVersionDO.getAppId(), applicationVersionDO.getAppCode(),
                applicationVersionDO.getAppName(), applicationVersionDO.getAppStatus());
        applicationVersionE.initApplicationVersionValueE(applicationVersionDO.getValueId());
        applicationVersionE.initApplicationVersionReadmeVById(applicationVersionDO.getReadmeValueId());
        return applicationVersionE;
    }

    @Override
    public ApplicationVersionDO entityToDo(ApplicationVersionE applicationVersionE) {
        ApplicationVersionDO applicationVersionDO = new ApplicationVersionDO();
        BeanUtils.copyProperties(applicationVersionE, applicationVersionDO);
        applicationVersionDO.setAppId(applicationVersionE.getApplicationE().getId());
        if (applicationVersionE.getApplicationVersionValueE() != null) {
            applicationVersionDO.setValueId(applicationVersionE.getApplicationVersionValueE().getId());
        }
        return applicationVersionDO;
    }

    @Override
    public ApplicationVersionRepDTO entityToDto(ApplicationVersionE entity) {
        ApplicationVersionRepDTO applicationVersionRepDTO = new ApplicationVersionRepDTO();
        BeanUtils.copyProperties(entity, applicationVersionRepDTO);
        applicationVersionRepDTO.setAppId(entity.getApplicationE().getId());
        applicationVersionRepDTO.setAppCode(entity.getApplicationE().getCode());
        applicationVersionRepDTO.setAppName(entity.getApplicationE().getName());
        applicationVersionRepDTO.setAppStatus(entity.getApplicationE().getActive());
        return applicationVersionRepDTO;
    }
}
