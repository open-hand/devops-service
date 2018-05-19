package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;

/**
 * Created by younger on 2018/4/3.
 */
public class ApplicationTemplateFactory {

    private ApplicationTemplateFactory() {
    }

    public static ApplicationTemplateE createApplicationTemplateE() {
        return ApplicationContextHelper.getSpringFactory().getBean(ApplicationTemplateE.class);
    }
}
