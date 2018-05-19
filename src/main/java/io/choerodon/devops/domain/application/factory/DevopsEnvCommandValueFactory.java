package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandValueE;

public class DevopsEnvCommandValueFactory {

    private DevopsEnvCommandValueFactory() {
    }

    public static DevopsEnvCommandValueE createDevopsEnvCommandE() {
        return ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvCommandValueE.class);
    }
}
