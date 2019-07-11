package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ApplicationTemplateRepDTO;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;

@Component
public class ApplicationTemplateRepConvertor implements ConvertorI<ApplicationTemplateE, Object, ApplicationTemplateRepDTO> {

    @Override
    public ApplicationTemplateRepDTO entityToDto(ApplicationTemplateE applicationTemplateE) {
        ApplicationTemplateRepDTO applicationTemplateDTO = new ApplicationTemplateRepDTO();
        BeanUtils.copyProperties(applicationTemplateE, applicationTemplateDTO);
        applicationTemplateDTO.setOrganizationId(applicationTemplateE.getOrganization().getId());
        return applicationTemplateDTO;
    }

}