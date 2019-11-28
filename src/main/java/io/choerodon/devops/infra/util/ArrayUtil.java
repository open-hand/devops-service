package io.choerodon.devops.infra.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: changzhuang
 * @Date: Create in 11:55 2018/9/26
 */
public class ArrayUtil {

    private ArrayUtil() {
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

    /**
     * 把单个对象存入list中
     *
     * @param obj 对象
     * @param <T> 泛型
     * @return 列表
     */
    public static <T> List<T> singleAsList(T obj) {
        List<T> list = new ArrayList<>();
        list.add(obj);
        return list;
    }
}
