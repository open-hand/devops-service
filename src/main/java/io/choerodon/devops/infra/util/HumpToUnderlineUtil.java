package io.choerodon.devops.infra.util;

import org.springframework.util.StringUtils;

/**
 * 驼峰转下划线工具类
 *
 * @author: 25499
 * @date: 2019/9/4 9:35
 */
public final class HumpToUnderlineUtil {
    private static final String UNDERLINE = "_";

    private HumpToUnderlineUtil() {
    }

    /**
     * 驼峰转下划线格式
     *
     * @param camelCase 驼峰格式字符串
     * @return 下划线格式字符串
     */
    public static String toUnderLine(String camelCase) {
        if (StringUtils.isEmpty(camelCase)) {
            return camelCase;
        }
        StringBuilder builder = new StringBuilder(camelCase);
        int upperCaseCharNumber = 0;
        for (int i = 0; i < camelCase.length(); i++) {
            if (Character.isUpperCase(camelCase.charAt(i))) {
                builder.insert(i + upperCaseCharNumber, UNDERLINE);
                // 删除大写字母
                builder.deleteCharAt(i + upperCaseCharNumber + 1);
                // 插入小写字母
                builder.insert(i + upperCaseCharNumber + 1, Character.toLowerCase(camelCase.charAt(i)));
                upperCaseCharNumber += 1;
            }
        }
        return builder.toString();
    }
}
