package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.ApplicationDTO;

/**
 * Created by younger on 2018/3/28.
 */
public class ApplicationValidator {

    private static final String SERVICE_PATTERN = "[a-zA-Z0-9_\\.][a-zA-Z0-9_\\-\\.]*[a-zA-Z0-9_\\-]|[a-zA-Z0-9_]";

    private ApplicationValidator() {
    }

    /**
     * 检查应用的name和code是否符合标准
     */
    public static void checkApplication(ApplicationDTO applicationDTO) {
        if (!Pattern.matches(SERVICE_PATTERN, applicationDTO.getCode())) {
            throw new CommonException("error.app.code.notMatch");
        }
    }
}
