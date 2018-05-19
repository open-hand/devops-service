package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationTemplateDTO;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;
import io.choerodon.devops.domain.application.factory.ApplicationTemplateFactory;

/**
 * Created by younger on 2018/3/27.
 */
@Component
public class ApplicationTemplateAssember implements ConvertorI<ApplicationTemplateE, Object, ApplicationTemplateDTO> {

    @Override
    public ApplicationTemplateE dtoToEntity(ApplicationTemplateDTO applicationTemplateDTO) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        BeanUtils.copyProperties(applicationTemplateDTO, applicationTemplateE);
        applicationTemplateE.initOrganization(applicationTemplateDTO.getOrganizationId());
        return applicationTemplateE;
    }


}
