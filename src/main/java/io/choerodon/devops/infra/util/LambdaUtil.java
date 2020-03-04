package io.choerodon.devops.infra.util;

import java.util.function.Consumer;


/**
 * @author zmf
 * @since 2/24/20
 */
public final class LambdaUtil {
    private LambdaUtil() {
    }

    private static final Consumer DO_NOTHING = no -> {
    };

    @SuppressWarnings("unchecked")
    public static <F> Consumer<F> doNothingConsumer() {
        return DO_NOTHING;
    }
}
