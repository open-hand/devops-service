package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.ApplicationE;

/**
 * Created by younger on 2018/4/4.
 */
public class ApplicationFactory {

    private ApplicationFactory() {
    }

    public static ApplicationE createApplicationE() {
        return ApplicationContextHelper.getSpringFactory().getBean(ApplicationE.class);
    }
}
