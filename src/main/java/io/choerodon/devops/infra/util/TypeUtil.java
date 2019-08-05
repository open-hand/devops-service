package io.choerodon.devops.infra.util;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by younger on 2018/3/29.
 */
public class TypeUtil {

    public static final String SEARCH_PARAM = "searchParam";
    public static final String PARAM = "param";
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
        if (obj == null) {
            return null;
        }
        return Integer.valueOf(String.valueOf(obj));
    }

    /**
     * obj转long类型
     */

    public static Long objToLong(Object obj) {
        if (obj == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(obj));
    }

    /**
     * obj转double类型
     */

    public static double objTodouble(Object obj) {
        if (obj == null) {
            return 0;
        }
        return Double.parseDouble(String.valueOf(obj));
    }

    /**
     * obj转int类型
     */

    public static int objToInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(obj));
    }

    /**
     * obj转boolean类型
     */

    public static Boolean objToBoolean(Object obj) {
        if (obj == null) {
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

    public static Map<String,Object> castMapParams(String params){
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put(TypeUtil.SEARCH_PARAM, null);
        mapParams.put(TypeUtil.PARAM, null);

        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            mapParams.put(TypeUtil.SEARCH_PARAM, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)));
            mapParams.put(TypeUtil.PARAM, TypeUtil.cast(maps.get(TypeUtil.PARAM)));
        }

        return mapParams;
    }
}
