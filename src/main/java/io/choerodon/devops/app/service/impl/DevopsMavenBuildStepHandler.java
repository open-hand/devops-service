package io.choerodon.devops.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 17:52
 */
@Service
public class DevopsMavenBuildStepHandler extends AbstractDevopsCiStepHandler {
    protected DevopsCiStepTypeEnum type = DevopsCiStepTypeEnum.MAVEN_BUILD;
}
