package io.choerodon.devops.infra.util;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/6/13 14:57
 */
public class SonarUtil {
    public static String caculateSqaleIndex(Long sqaleIndex) {
        double day = sqaleIndex == null ? 0 : TypeUtil.objTodouble(sqaleIndex) / 480;
        double hour = sqaleIndex == null ? 0 : TypeUtil.objTodouble(sqaleIndex) / 60;
        if (day >= 1) {
            return String.format("%sd", Math.round(day));
        } else if (hour >= 1) {
            return String.format("%sh", Math.round(hour));
        } else {
            return String.format("%s%s", Math.round(TypeUtil.objTodouble(sqaleIndex == null ? 0 : sqaleIndex)), sqaleIndex == null ? "" : "min");
        }
    }
}
