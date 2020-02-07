package io.choerodon.devops.infra.util;

import java.util.UUID;

public class GenerateUUID {

    private GenerateUUID() {
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成随机的用于gitlab用户的密码
     *
     * @return 生成的随机密码
     */
    public static String generateRandomGitlabPassword() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
    }
}
