package io.choerodon.devops.infra.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;

/**
 * feign param util
 *
 * @author superlee
 * @since 2019-07-11
 */
public class FeignParamUtils {

    private FeignParamUtils() {
    }

    /**
     * 将PageRequest编码为map
     *
     * @param pageable 分页参数
     * @return hashmap
     */
    public static Map<String, Object> encodePageRequest(PageRequest pageable) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("page", pageable.getPage());
        map.put("size", pageable.getSize());
        Sort sort = pageable.getSort();
        if (sort != null) {
            List<String> values = new ArrayList<>();
            for (Sort.Order order : sort) {
                String value = order.getProperty() + "," + order.getDirection();
                values.add(value);
            }
            if (!values.isEmpty()) {
                map.put("sort", values);
            }
        }
        return map;
    }

}
