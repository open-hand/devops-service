package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD
=======
<<<<<<< HEAD
import io.choerodon.devops.api.vo.ApplicationUpdateVO;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.entity.ApplicationE;
=======
>>>>>>> 99504a39d606d3005354e0b1bdcb50530cde6afd
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
>>>>>>> [IMP] 修改AppControler重构
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
