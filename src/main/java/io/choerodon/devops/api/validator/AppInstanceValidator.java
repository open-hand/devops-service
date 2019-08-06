package io.choerodon.devops.api.validator;

import java.util.regex.Pattern;

import io.choerodon.core.exception.CommonException;

/**
 * Created by n!Ck
 * Date: 2018/11/20
 * Time: 9:51
 * Description:
 */
public class AppInstanceValidator {
    //appServiceInstance name
    private static final String NAME_PATTERN = "[a-z]([-a-z0-9]*[a-z0-9])?";

    private AppInstanceValidator() {
    }


    public static void checkName(String name) {
        if (!Pattern.matches(NAME_PATTERN, name)) {
            throw new CommonException("error.app.instance.name.notMatch");
        }
    }
}
