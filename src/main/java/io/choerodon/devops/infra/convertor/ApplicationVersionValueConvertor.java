package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.ApplicationVersionValueE;
import io.choerodon.devops.domain.application.factory.ApplicationVersionValueFactory;
import io.choerodon.devops.infra.dto.ApplicationVersionValueDTO;

@Component
public class ApplicationVersionValueConvertor implements ConvertorI<ApplicationVersionValueE, ApplicationVersionValueDTO, Object> {

    @Override
    public ApplicationVersionValueE doToEntity(ApplicationVersionValueDTO applicationVersionValueDTO) {
        ApplicationVersionValueE applicationVersionValueE = ApplicationVersionValueFactory.create();
        BeanUtils.copyProperties(applicationVersionValueDTO, applicationVersionValueE);
        return applicationVersionValueE;
    }

    @Override
    public ApplicationVersionValueDTO entityToDo(ApplicationVersionValueE applicationVersionValueE) {
        ApplicationVersionValueDTO applicationVersionValueDTO = new ApplicationVersionValueDTO();
        BeanUtils.copyProperties(applicationVersionValueE, applicationVersionValueDTO);
        return applicationVersionValueDTO;
    }
}
