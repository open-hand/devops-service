package io.choerodon.devops.app.eventhandler.pipeline.step;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * Created by wangxiang on 2021/12/20
 */
@Service
public class DevopsNodeJsBuildStepHandler extends AbstractDevopsCiStepHandler {

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.NODE_JS_BUILD;
    }
}
