package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;

public class DevopsEnvCommandFactory {

    private DevopsEnvCommandFactory() {
    }

    public static DevopsEnvCommandE createDevopsEnvCommandE() {
        return ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvCommandE.class);
    }
}
