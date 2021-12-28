package io.choerodon.devops.app.eventhandler.pipeline.step;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈上传jar包相关步骤，由于代码逻辑和Maven发布一致，所以直接继承DevopsCiMavenPublishStepHandler〉
 *
 * @author wanghao
 * @since 2021/12/15 14:25
 */
@Service
public class DevopsCiUploadJarStepHandler extends DevopsCiMavenPublishStepHandler {


    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.UPLOAD_JAR;
    }
}
