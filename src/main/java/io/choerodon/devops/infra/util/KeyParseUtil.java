package io.choerodon.devops.infra.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sheep on 2019/7/25.
 */
public class KeyParseUtil {

    private KeyParseUtil() {
    }

    public static boolean matchPattern(String key) {
        String[] pairs = key.split("\\.");
        if (pairs.length == 0) {
            return false;
        }
        for (String pair : pairs) {
            String[] content = pair.split(":");
            if (content.length != 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将形如 env:1.namespace:c7n-system 的key解析为map
     * key的构建规则（由这个类文件中解析的逻辑概括总结）：
     * ([^.]+:[^.]+\.)(资源类型:资源名称)
     *
     * @param key 待解析的key
     * @return map
     */
    public static Map<String, String> parseKey(String key) {
        return parseKeyInOrder(key, false);
    }

    /**
     * 将形如 env:1.namespace:c7n-system 的key解析为map
     * key的构建规则（由这个类文件中解析的逻辑概括总结）：
     * ([^.]+:[^.]+\.)(资源类型:资源名称)
     *
     * @param key 待解析的key
     * @return map
     */
    public static Map<String, String> parseKeyInOrder(String key) {
        return parseKeyInOrder(key, true);
    }

    private static Map<String, String> parseKeyInOrder(String key, boolean inOrder) {
        Map<String, String> keyMap;
        if (inOrder) {
            keyMap = new LinkedHashMap<>();
        } else {
            keyMap = new HashMap<>();
        }
        String[] pairs = key.split("\\.");
        for (String pair : pairs) {
            String[] content = pair.split(":");
            if (content.length != 2) {
                continue;
            }
            keyMap.put(content[0], content[1]);
        }
        return keyMap;
    }

    public static String getValue(String key, String name) {
        return parseKey(key).get(name);
    }

    public static String getNamespace(String key) {
        return getValue(key, "env");
    }

    public static String getCommit(String key) {
        return getValue(key, "commit");
    }

    public static String getReleaseName(String key) {
        return getValue(key, "release");
    }

    public static String getResourceName(String key) {
        String[] pairs = key.split("\\.");
        String content = pairs[pairs.length - 1];
        return content.split(":")[1];
    }

    public static String getResourceType(String key) {
        String[] pairs = key.split("\\.");
        String content = pairs[pairs.length - 1];
        return content.split(":")[0];
    }

}
