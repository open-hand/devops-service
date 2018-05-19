package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;

/**
 * Created by younger on 2018/4/9.
 */
public class DevopsEnvironmentFactory {

    private DevopsEnvironmentFactory() {
    }

    public static DevopsEnvironmentE createDevopsEnvironmentE() {
        return ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvironmentE.class);
    }
}
