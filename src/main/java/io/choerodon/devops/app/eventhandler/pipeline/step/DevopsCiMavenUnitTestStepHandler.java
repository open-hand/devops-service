package io.choerodon.devops.app.eventhandler.pipeline.step;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/14 17:52
 */
@Service
public class DevopsCiMavenUnitTestStepHandler extends DevopsCiMavenBuildStepHandler {

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.MAVEN_UNIT_TEST;
    }
}
