package io.choerodon.devops.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 17:33
 */
@Service
public class DevopsChartStepHandler extends AbstractDevopsCiStepHandler {

    protected DevopsCiStepTypeEnum type = DevopsCiStepTypeEnum.UPLOAD_CHART;


    @Override
    public String getType() {
        return type.value();
    }
}
