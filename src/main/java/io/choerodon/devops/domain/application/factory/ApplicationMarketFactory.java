package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.ApplicationMarketE;

public class ApplicationMarketFactory {

    private ApplicationMarketFactory() {
    }

    public static ApplicationMarketE create() {
        return ApplicationContextHelper.getSpringFactory().getBean(ApplicationMarketE.class);
    }
}
