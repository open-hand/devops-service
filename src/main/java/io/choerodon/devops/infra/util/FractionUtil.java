package io.choerodon.devops.infra.util;

/**
 * @author zmf
 * @since 2020/9/30
 */
public final class FractionUtil {
    private FractionUtil() {
    }

    /**
     * 保留几位小数
     *
     * @param n 保留几位小数
     * @return 四舍五入保留指定位数小数的结果
     */
    public static double fraction(double original, int n) {
        if (n < 0) {
            n = 0;
        }
        double temp = Math.pow(10, n);
        return Math.round(original * temp) / temp;
    }
}
