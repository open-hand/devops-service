package io.choerodon.devops.app.service.impl;

import java.util.Set;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/15 10:23
 */
public class DevopsCiNodeJsUnitTestStepHandler extends AbstractDevopsCiStepHandler {
    @Override
    public void fillConfigInfo(DevopsCiStepVO devopsCiStepVO) {

    }

    @Override
    protected void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {

    }

    @Override
    protected void batchDeleteConfig(Set<Long> stepIds) {

    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.NODE_JS_UNIT_TEST;
    }
}
