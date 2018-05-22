package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationDTO;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.factory.ApplicationFactory;

/**
 * Created by younger on 2018/3/28.
 */
@Component
public class ApplicationAssember implements ConvertorI<ApplicationE, Object, ApplicationDTO> {

    @Override
    public ApplicationE dtoToEntity(ApplicationDTO applicationDTO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        BeanUtils.copyProperties(applicationDTO, applicationE);
        applicationE.initProjectE(applicationDTO.getProjectId());
        if (applicationDTO.getApplictionTemplateId() != null) {
            applicationE.initApplicationTemplateE(applicationDTO.getApplictionTemplateId());
        }
        return applicationE;
    }

    @Override
    public ApplicationDTO entityToDto(ApplicationE applicationE) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        BeanUtils.copyProperties(applicationE, applicationDTO);
        if(applicationE.getProjectE()!=null){
            applicationDTO.setProjectId(applicationE.getProjectE().getId());
        }
        if(applicationE.getApplicationTemplateE()!=null){
            applicationDTO.setApplictionTemplateId(applicationE.getApplicationTemplateE().getId());
        }
        return applicationDTO;
    }

}
