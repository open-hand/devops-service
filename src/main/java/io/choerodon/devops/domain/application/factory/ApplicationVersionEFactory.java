package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;

public class ApplicationVersionEFactory {

    private ApplicationVersionEFactory() {
    }

    public static ApplicationVersionE create() {
        return ApplicationContextHelper.getSpringFactory().getBean(ApplicationVersionE.class);
    }
}
