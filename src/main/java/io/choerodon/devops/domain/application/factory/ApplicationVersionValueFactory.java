package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.ApplicationVersionValueE;

public class ApplicationVersionValueFactory {

    private ApplicationVersionValueFactory() {
    }

    public static ApplicationVersionValueE create() {
        return ApplicationContextHelper.getSpringFactory().getBean(ApplicationVersionValueE.class);
    }
}
