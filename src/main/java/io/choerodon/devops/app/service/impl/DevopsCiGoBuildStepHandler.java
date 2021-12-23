package io.choerodon.devops.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/16 14:39
 */
@Service
public class DevopsCiGoBuildStepHandler extends AbstractDevopsCiStepHandler {

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.GO_BUILD;
    }
}
