package io.choerodon.devops.infra.common.util;

/**
 * spock 代替 mysql 的函数定义类
 * <p>
 * Created by n!Ck
 * Date: 2018/11/7
 * Time: 14:23
 * Description:
 */
public class MybatisFunctionTestUtil {

    private MybatisFunctionTestUtil() {
        new MybatisFunctionTestUtil();
    }

    public static String binaryFunction(String name) {
        return name;
    }
}
