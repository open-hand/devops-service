package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsIngressDTO;

/**
 * Created by Zenger on 2018/4/26.
 */
public class DevopsIngressValidator {

    //ingress name
    private static final String NAME_PATTERN = "[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*";
    // ingress subdomain
    private static final String SUB_PATH_PATTERN = "[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*";

    private DevopsIngressValidator() {
    }

    /**
     * 参数校验
     */
    public static void checkAppVersion(DevopsIngressDTO devopsIngressDTO) {
        if (!Pattern.matches(NAME_PATTERN, devopsIngressDTO.getName())) {
            throw new CommonException("error.ingress.name.notMatch");
        }
    }

    /**
     * 参数校验
     */
    public static void checkPath(String path) {
        if (!Pattern.matches(NAME_PATTERN, path)) {
            throw new CommonException("error.ingress.subPath.notMatch");
        }
    }
}
