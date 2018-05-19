package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.ApplicationVersionValueE;
import io.choerodon.devops.domain.application.factory.ApplicationVersionValueFactory;
import io.choerodon.devops.infra.dataobject.ApplicationVersionValueDO;

@Component
public class ApplicationVersionValueConvertor implements ConvertorI<ApplicationVersionValueE, ApplicationVersionValueDO, Object> {

    @Override
    public ApplicationVersionValueE doToEntity(ApplicationVersionValueDO applicationVersionValueDO) {
        ApplicationVersionValueE applicationVersionValueE = ApplicationVersionValueFactory.create();
        BeanUtils.copyProperties(applicationVersionValueDO, applicationVersionValueE);
        return applicationVersionValueE;
    }

    @Override
    public ApplicationVersionValueDO entityToDo(ApplicationVersionValueE applicationVersionValueE) {
        ApplicationVersionValueDO applicationVersionValueDO = new ApplicationVersionValueDO();
        BeanUtils.copyProperties(applicationVersionValueE, applicationVersionValueDO);
        return applicationVersionValueDO;
    }
}
