package io.choerodon.devops.infra.util;

import java.util.*;

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

    /**
     * 将所有数组元素按顺序加入到队尾
     *
     * @param queue    队列
     * @param elements 元素数组
     * @param <T>      类型
     */
    public static <T> void offerAllToQueue(Queue<T> queue, T[] elements) {
        if (elements != null) {
            for (T e : elements) {
                if (e != null) {
                    queue.offer(e);
                }
            }
        }
    }
}
