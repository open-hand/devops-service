package io.choerodon.devops.infra.util;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;

/**
 * @author zmf
 * @since 2020/7/22
 */
public class ExceptionUtil {
    /**
     * 包装一般的异常为 {@link DevopsCiInvalidException}, 用于返回非2xx的状态码
     *
     * @param runnable 执行的逻辑
     */
    public static void wrapExWithCiEx(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            if (e instanceof DevopsCiInvalidException) {
                throw e;
            }
            if (e instanceof CommonException) {
                throw new DevopsCiInvalidException(((CommonException) e).getCode(), e, ((CommonException) e).getParameters());
            }
            throw new DevopsCiInvalidException(e);
        }
    }
}
