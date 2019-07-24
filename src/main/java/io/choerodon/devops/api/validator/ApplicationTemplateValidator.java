package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ApplicationTemplateVO;

/**
 * Created by younger on 2018/4/4.
 */
public class ApplicationTemplateValidator {
    private static final String SERVICE_PATTERN = "[a-zA-Z0-9_\\.][a-zA-Z0-9_\\-\\.]*[a-zA-Z0-9_\\-]|[a-zA-Z0-9_]";

    private ApplicationTemplateValidator() {
    }

    /**
     * 检查应用的name和code是否符合标准
     */
    public static void checkApplicationTemplate(ApplicationTemplateVO applicationTemplateVO) {
        if (!Pattern.matches(SERVICE_PATTERN, applicationTemplateVO.getCode())) {
            throw new CommonException("error.template.code.notMatch");
        }
    }
}
