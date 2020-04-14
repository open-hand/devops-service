package io.choerodon.devops.infra.util;

import java.util.*;

import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
     * @param pageable
     * @return
     */
    public static Map<String, Object> encodePageRequest(PageRequest pageable) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("page", pageable.getPageNumber());
        map.put("size", pageable.getPageSize());
        Sort sort = pageable.getSort();
        if (sort != null) {
            List<String> values = new ArrayList<>();
            Iterator<Sort.Order> iterator = sort.iterator();
            while (iterator.hasNext()) {
                Sort.Order order = iterator.next();
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
