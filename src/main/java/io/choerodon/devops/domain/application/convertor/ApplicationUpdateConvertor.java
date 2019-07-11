package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD
=======
import io.choerodon.devops.api.vo.ApplicationUpdateVO;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.factory.ApplicationFactory;

@Component
public class ApplicationUpdateConvertor implements ConvertorI<ApplicationE, Object, ApplicationUpdateVO> {

    @Override
    public ApplicationE dtoToEntity(ApplicationUpdateVO applicationUpdateDTO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        BeanUtils.copyProperties(applicationUpdateDTO, applicationE);
        return applicationE;
    }

}
