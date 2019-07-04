package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsAppShareE;

public class ApplicationMarketFactory {

    private ApplicationMarketFactory() {
    }

    public static DevopsAppShareE create() {
        return ApplicationContextHelper.getSpringFactory().getBean(DevopsAppShareE.class);
    }
}
