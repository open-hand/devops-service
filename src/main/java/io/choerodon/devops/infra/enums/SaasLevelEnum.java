package io.choerodon.devops.infra.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/4/13
 * @Modified By:
 */
public enum SaasLevelEnum {
    STANDARD("标准版"),
    FREE("免费版"),
    SENIOR("高级版");

    private final String levelValue;

    SaasLevelEnum(String levelValue) {
        this.levelValue = levelValue;
    }

    public String getLevelValue() {
        return levelValue;
    }

    private static final Map<String, SaasLevelEnum> enumMap;

    static {
        enumMap = new HashMap<>();
        for (SaasLevelEnum value : SaasLevelEnum.values()) {
            enumMap.put(value.getLevelValue(), value);
        }
    }
}
