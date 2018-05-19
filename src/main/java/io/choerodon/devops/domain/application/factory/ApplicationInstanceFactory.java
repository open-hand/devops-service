package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE;

/**
 * Created by Zenger on 2018/4/12.
 */
public class ApplicationInstanceFactory {

    private ApplicationInstanceFactory() {
    }

    public static ApplicationInstanceE create() {
        return ApplicationContextHelper.getSpringFactory().getBean(ApplicationInstanceE.class);
    }
}
