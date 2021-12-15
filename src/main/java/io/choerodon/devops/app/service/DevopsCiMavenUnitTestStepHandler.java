package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/14 17:52
 */
public class DevopsCiMavenUnitTestStepHandler extends AbstractDevopsCiStepHandler {
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
        return DevopsCiStepTypeEnum.MAVEN_UNIT_TEST;
    }
}
