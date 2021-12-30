package io.choerodon.devops.app.eventhandler.pipeline.step;

import org.springframework.stereotype.Component;

import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/30 18:10
 */
@Component
public class DevopsCiNpmUploadStepHandler extends AbstractDevopsCiStepHandler {

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.NPM_UPLOAD;
    }
}
