package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvResourceE;

/**
 * Created by younger on 2018/4/24.
 */
public class DevopsInstanceResourceFactory {

    private DevopsInstanceResourceFactory() {
    }

    public static DevopsEnvResourceE createDevopsInstanceResourceE() {
        return ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvResourceE.class);
    }
}
