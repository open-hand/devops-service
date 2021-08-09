package io.choerodon.devops.infra.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MiddlewareAppServiceName {
    public static final Map<String, String> MIDDLE_APP_SERVICE_NAME_MAP;

    /**
     * 中间件名称映射
     * key: marketApplicationName-marketServiceName
     * value: 展示出来的名称
     */
    static{
        Map<String,String> map=new HashMap<>();
        map.put("Redis-standalone","Redis单机版");
        map.put("Redis-sentinel","Redis哨兵版");
        map.put("MySQL-standalone","MySQL单机版");
        MIDDLE_APP_SERVICE_NAME_MAP= Collections.unmodifiableMap(map);
    }
}
