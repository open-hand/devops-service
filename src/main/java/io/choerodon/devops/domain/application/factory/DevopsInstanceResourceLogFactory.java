package io.choerodon.devops.domain.application.factory;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandLogE;

/**
 * Created by younger on 2018/4/24.
 */
public class DevopsInstanceResourceLogFactory {

    private DevopsInstanceResourceLogFactory() {
    }

    public static DevopsEnvCommandLogE createDevopsInstanceResourceLogE() {
        return ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvCommandLogE.class);
    }


}
