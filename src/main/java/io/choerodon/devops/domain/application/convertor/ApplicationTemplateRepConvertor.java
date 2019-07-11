package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD
=======
import io.choerodon.devops.api.vo.ApplicationTemplateRepVO;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;

@Component
public class ApplicationTemplateRepConvertor implements ConvertorI<ApplicationTemplateE, Object, ApplicationTemplateRepVO> {

    @Override
    public ApplicationTemplateRepVO entityToDto(ApplicationTemplateE applicationTemplateE) {
        ApplicationTemplateRepVO applicationTemplateDTO = new ApplicationTemplateRepVO();
        BeanUtils.copyProperties(applicationTemplateE, applicationTemplateDTO);
        applicationTemplateDTO.setOrganizationId(applicationTemplateE.getOrganization().getId());
        return applicationTemplateDTO;
    }

}