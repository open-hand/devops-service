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
        return UUID.randomUUID().toString().replaceAll("-", "a").substring(0, 10);
    }

    /**
     * 生成随机的字符串用于环境本地目录
     *
     * @return 10位的随机字符串
     */
    public static String generateRandomString() {
        return UUID.randomUUID().toString().replaceAll("-", "a").substring(0, 10);
    }
}
