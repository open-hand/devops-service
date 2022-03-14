package io.choerodon.devops.app.eventhandler.pipeline.step;

import org.springframework.stereotype.Component;

import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/2/15 9:30
 */
@Component
public class DevopsCiGeneralGeneralUnitTestStepHandler extends AbstractDevopsCiStepHandler {

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.GENERAL_UNIT_TEST;
    }
}
