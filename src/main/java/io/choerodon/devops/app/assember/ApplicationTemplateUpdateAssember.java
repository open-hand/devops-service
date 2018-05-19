package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationTemplateUpdateDTO;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;
import io.choerodon.devops.domain.application.factory.ApplicationTemplateFactory;

/**
 * Created by younger on 2018/4/2.
 */
@Component
public class ApplicationTemplateUpdateAssember implements ConvertorI<ApplicationTemplateE, Object, ApplicationTemplateUpdateDTO> {

    @Override
    public ApplicationTemplateE dtoToEntity(ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        BeanUtils.copyProperties(applicationTemplateUpdateDTO, applicationTemplateE);
        return applicationTemplateE;
    }

}
