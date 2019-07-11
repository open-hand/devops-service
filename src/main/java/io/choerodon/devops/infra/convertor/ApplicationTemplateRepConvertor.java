package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD
=======
<<<<<<< HEAD
import io.choerodon.devops.api.vo.ApplicationTemplateRepVO;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;
=======
>>>>>>> 99504a39d606d3005354e0b1bdcb50530cde6afd
import io.choerodon.devops.api.vo.iam.entity.ApplicationTemplateE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/ApplicationTemplateRepConvertor.java
>>>>>>> [IMP] 修改AppControler重构
=======
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/ApplicationTemplateRepConvertor.java

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