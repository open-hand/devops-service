package io.choerodon.devops.infra.common.util;

import java.util.UUID;

public class GenerateUUID {

    private GenerateUUID() {
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
