package io.choerodon.devops.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.common.util.enums.AppResourceType;

import static io.choerodon.devops.infra.common.util.enums.AppResourceType.*;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
public class AppResourceValidator {

    private AppResourceValidator() {
    }

    public static String checkResourceType(String type) {
        AppResourceType checkType = AppResourceType.valueOf(type.toUpperCase());
        if (!checkType.equals(AppResourceType.SERVICE) && !checkType.equals(AppResourceType.SECRET) &&
                !checkType.equals(AppResourceType.CONFIGMAP) && !checkType.equals(AppResourceType.INGRESS)) {
            throw new CommonException("error.app.resource.type");
        }
        return checkType.getResourceType();
    }
}
