package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ApplicationTemplateUpdateDTO;
import io.choerodon.devops.api.vo.iam.entity.ApplicationTemplateE;
import io.choerodon.devops.domain.application.factory.ApplicationTemplateFactory;

@Component
public class ApplicationTemplateUpdateConvertor implements ConvertorI<ApplicationTemplateE, Object, ApplicationTemplateUpdateDTO> {

    @Override
    public ApplicationTemplateE dtoToEntity(ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        BeanUtils.copyProperties(applicationTemplateUpdateDTO, applicationTemplateE);
        return applicationTemplateE;
    }

}