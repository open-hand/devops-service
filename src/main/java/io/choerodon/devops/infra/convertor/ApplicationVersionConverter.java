package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ApplicationVersionRepDTO;
import io.choerodon.devops.api.vo.iam.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.factory.ApplicationVersionEFactory;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;

@Component
public class ApplicationVersionConverter implements ConvertorI<ApplicationVersionE, ApplicationVersionDTO, ApplicationVersionRepDTO> {

    @Override
    public ApplicationVersionE doToEntity(ApplicationVersionDTO applicationVersionDTO) {
        ApplicationVersionE applicationVersionE = ApplicationVersionEFactory.create();
        BeanUtils.copyProperties(applicationVersionDTO, applicationVersionE);
        applicationVersionE.initApplicationE(applicationVersionDTO.getAppId(), applicationVersionDTO.getAppCode(),
                applicationVersionDTO.getAppName(), applicationVersionDTO.getAppStatus());
        applicationVersionE.initApplicationVersionValueE(applicationVersionDTO.getValueId());
        applicationVersionE.initApplicationVersionReadmeVById(applicationVersionDTO.getReadmeValueId());
        return applicationVersionE;
    }

    @Override
    public ApplicationVersionDTO entityToDo(ApplicationVersionE applicationVersionE) {
        ApplicationVersionDTO applicationVersionDTO = new ApplicationVersionDTO();
        BeanUtils.copyProperties(applicationVersionE, applicationVersionDTO);
        applicationVersionDTO.setAppId(applicationVersionE.getApplicationE().getId());
        if (applicationVersionE.getApplicationVersionValueE() != null) {
            applicationVersionDTO.setValueId(applicationVersionE.getApplicationVersionValueE().getId());
        }
        return applicationVersionDTO;
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
