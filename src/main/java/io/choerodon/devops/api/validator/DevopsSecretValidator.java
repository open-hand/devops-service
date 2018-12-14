package io.choerodon.devops.api.validator;

import java.util.Set;
import java.util.regex.Pattern;

import io.choerodon.core.exception.CommonException;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午11:52
 * Description:
 */
public class DevopsSecretValidator {
    // secret name
    private static final String NAME_PATTERN = "[a-z]([-a-z0-9]*[a-z0-9])?";
    // secret key name
    private static final String KEY_NAME_PATTERN = "/[^0-9A-Za-z\\.\\-\\_]/";

    private DevopsSecretValidator() {
    }

    public static void checkName(String name) {
        if (!Pattern.matches(NAME_PATTERN, name)) {
            throw new CommonException("error.secret.name.notMatch");
        }
    }

    // 此处的正则是反向筛选的，如果匹配正则，表示是非法的keyname
    public static void checkKeyName(Set<String> keySet) {
        keySet.forEach(e -> {
            if (Pattern.matches(KEY_NAME_PATTERN, e)) {
                throw new CommonException("error.secret.key.name.notMatch");
            }
        });
    }
}
