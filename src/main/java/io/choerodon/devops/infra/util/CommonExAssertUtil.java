package io.choerodon.devops.infra.util;

import io.choerodon.core.exception.CommonException;

/**
 * 断言辅助类, 不满足断言, 抛出{@link CommonException}
 *
 * @author zmf
 * @since 2020/5/26
 */
public final class CommonExAssertUtil {
    private CommonExAssertUtil() {
    }

    /**
     * 断言为true
     *
     * @param value         boolean结果值
     * @param errorCode     错误码
     * @param messageParams 消息参数
     */
    public static void assertTrue(boolean value, String errorCode, Object... messageParams) {
        if (!value) {
            throw new CommonException(errorCode, messageParams);
        }
    }

    /**
     * 断言不为空
     *
     * @param value         待判断的类
     * @param errorCode     错误码
     * @param messageParams 消息参数
     */
    public static void assertNotNull(Object value, String errorCode, Object... messageParams) {
        if (value == null) {
            throw new CommonException(errorCode, messageParams);
        }
    }
}
