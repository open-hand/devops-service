package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationTemplateRepDTO;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;

/**
 * Created by younger on 2018/4/2.
 */
@Component
public class ApplicationTemplateRepAssember implements ConvertorI<ApplicationTemplateE, Object, ApplicationTemplateRepDTO> {

    @Override
    public ApplicationTemplateRepDTO entityToDto(ApplicationTemplateE applicationTemplateE) {
        ApplicationTemplateRepDTO applicationTemplateDTO = new ApplicationTemplateRepDTO();
        BeanUtils.copyProperties(applicationTemplateE, applicationTemplateDTO);
        applicationTemplateDTO.setOrganizationId(applicationTemplateE.getOrganization().getId());
        return applicationTemplateDTO;
    }

}
