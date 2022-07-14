package io.choerodon.devops.app.eventhandler.pipeline.step;

import org.springframework.stereotype.Component;

import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/7/13 17:21
 */
@Component
public class DevopsCiPublishAppVersionStepHandler extends AbstractDevopsCiStepHandler {
    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.PUBLISH_APP_VERSION;
    }
}
