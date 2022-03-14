package io.choerodon.devops.app.eventhandler.pipeline.step;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/15 10:23
 */
@Service
public class DevopsCiGoUnitTestStepHandler extends AbstractDevopsCiStepHandler {

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.GO_UNIT_TEST;
    }
}
