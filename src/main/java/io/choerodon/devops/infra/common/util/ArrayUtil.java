package io.choerodon.devops.infra.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: changzhuang
 * @Date: Create in 11:55 2018/9/26
 */
public class ArrayUtil {
    public ArrayUtil() {
    }

    //判断集合是否为空
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    //判断Map是否为空
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    //判断数组是否为空
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    //判断List是否为空
    public static boolean isEmpty(List<Object> list) {
        return list == null || list.isEmpty();
    }
}
