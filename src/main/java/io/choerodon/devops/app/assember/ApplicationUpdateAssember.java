package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationUpdateDTO;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.factory.ApplicationFactory;

/**
 * Created by younger on 2018/4/10.
 */
@Component
public class ApplicationUpdateAssember implements ConvertorI<ApplicationE, Object, ApplicationUpdateDTO> {

    @Override
    public ApplicationE dtoToEntity(ApplicationUpdateDTO applicationUpdateDTO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        BeanUtils.copyProperties(applicationUpdateDTO, applicationE);
        return applicationE;
    }

}
