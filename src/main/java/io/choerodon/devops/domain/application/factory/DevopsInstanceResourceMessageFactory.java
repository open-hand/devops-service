package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvResourceDetailE;

/**
 * Created by younger on 2018/4/24.
 */
public class DevopsInstanceResourceMessageFactory {

    private DevopsInstanceResourceMessageFactory() {
    }

    public static DevopsEnvResourceDetailE createDevopsInstanceResourceMessageE() {
        return ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvResourceDetailE.class);
    }
}
