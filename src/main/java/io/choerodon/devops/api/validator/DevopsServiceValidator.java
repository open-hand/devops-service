package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsServiceReqDTO;

/**
 * Created by Zenger on 2018/4/26.
 */
public class DevopsServiceValidator {

    //仅支持ip4地址
    private static final String IP_PATTERN = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

    //service name
    private static final String NAME_PATTERN = "[a-z]([-a-z0-9]*[a-z0-9])?";

    /**
     * 参数校验
     */
    public static void checkAppVersion(DevopsServiceReqDTO devopsServiceReqDTO) {
        if (!Pattern.matches(NAME_PATTERN, devopsServiceReqDTO.getName())) {
            throw new CommonException("error.network.name.notMatch");
        }

        if (!StringUtils.isEmpty(devopsServiceReqDTO.getExternalIp())) {
            if (!Pattern.matches(IP_PATTERN, devopsServiceReqDTO.getExternalIp())) {
                throw new CommonException("error.externalIp.notMatch");
            }
        }
    }
}
