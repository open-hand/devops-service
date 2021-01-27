package io.choerodon.devops.infra.util;

import java.lang.reflect.Field;
import java.util.*;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by younger on 2018/3/29.
 */
public class TypeUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeUtil.class);
    public static final String SEARCH_PARAM = "searchParam";
    public static final String PARAMS = "params";
    private static final Gson gson = new Gson();

    private TypeUtil() {
    }

    /**
     * obj转string类型
     */

    public static String objToString(Object obj) {
        if (obj == null) {
            return null;
        }
        return String.valueOf(obj);
    }

    /**
     * obj转integer类型
     */

    public static Integer objToInteger(Object obj) {
        if (obj == null || "".equals(obj)) {
            return null;
        }
        return Integer.valueOf(String.valueOf(obj));
    }

    /**
     * obj转long类型
     */

    public static Long objToLong(Object obj) {
        if (obj == null || "".equals(obj)) {
            return null;
        }
        return Long.valueOf(String.valueOf(obj));
    }

    /**
     * obj转double类型
     */

    public static double objTodouble(Object obj) {
        if (obj == null || "".equals(obj)) {
            return 0;
        }
        return Double.parseDouble(String.valueOf(obj));
    }

    /**
     * obj转int类型
     */

    public static int objToInt(Object obj) {
        if (obj == null || "".equals(obj)) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(obj));
    }

    /**
     * obj转boolean类型
     */

    public static Boolean objToBoolean(Object obj) {
        if (obj == null || "".equals(obj)) {
            return false;
        }
        return Boolean.valueOf(String.valueOf(obj));
    }

    /**
     * 对象转换
     *
     * @param obj obj
     * @param <T> t
     * @return t
     */
    public static <T> T cast(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return (T) obj;
        }
    }

    public static Map<String, Object> castMapParams(String params) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put(TypeUtil.SEARCH_PARAM, null);
        mapParams.put(TypeUtil.PARAMS, null);

        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = gson.fromJson(params, Map.class);
            mapParams.put(TypeUtil.SEARCH_PARAM, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)));
            mapParams.put(TypeUtil.PARAMS, TypeUtil.cast(maps.get(TypeUtil.PARAMS)));
        }

        return mapParams;
    }

    /**
     * 判断对象中属性值是否全为空
     *
     * @param object
     * @return
     */
    public static boolean checkObjAllFieldsIsNull(Object object) {
        if (null == object) {
            return true;
        }

        try {
            for (Field f : object.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.get(object) != null && StringUtils.isNotBlank(f.get(object).toString())) {
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.info("exception", e);
        }
        return true;
    }

    public static <T> List<T> getListWithType(Map<Class<T>, List> map, Class<T> key) {
        return map.get(key);
    }

    /**
     * 将Long转为long，null值转为0
     *
     * @param value 对象
     * @return long值
     */
    public static long wrappedLongToPrimitive(@Nullable Long value) {
        return value == null ? 0 : value;
    }

    public static List<Long> stringArrayToLong(String[] objects) {
        if (objects == null || objects.length == 0) {
            return Collections.emptyList();
        }
        List<Long> list = new ArrayList<>();
        for (String object : objects) {
            list.add(Long.valueOf(object));
        }
        return list;
    }
}
