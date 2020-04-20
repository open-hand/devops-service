package io.choerodon.devops.api.validator;

import io.choerodon.devops.api.vo.DevopsCiPipelineVO;

/**
 * @author zmf
 * @since 20-4-20
 */
public class DevopsCiPipelineAdditionalValidator {
    private DevopsCiPipelineAdditionalValidator() {
    }

    /**
     * 是对JSR303无法校验的部分进行补充性的校验
     *
     * @param devopsCiPipelineVO 流水线数据
     */
    public static void additionalCheckPipeline(DevopsCiPipelineVO devopsCiPipelineVO) {
        // TODO
    }
}
