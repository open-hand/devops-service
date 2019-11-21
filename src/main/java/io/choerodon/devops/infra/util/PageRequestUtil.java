package io.choerodon.devops.infra.util;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:58 2019/4/4
 * Description:
 */
public class PageRequestUtil {
    private static final String ONE_SPACE = " ";

    private PageRequestUtil() {
    }

    public static String checkSortIsEmpty(Pageable pageable) {
        String index = "";
        if (pageable.getSort() == null) {
            index = "true";
        }
        return index;
    }


    public static String getOrderBy(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort != null) {
            return Lists.newArrayList(pageable.getSort().iterator()).stream()
                    .map(t -> HumpToUnderlineUtil.toUnderLine(t.getProperty()) + " " + t.getDirection())
                    .collect(Collectors.joining(","));
        }
        return "";
    }

    /**
     * 获取排序SQL字符串
     *
     * @param sort            {@link Pageable#getSort()}中的sort对象
     * @param orderByFieldMap 前端传入的字段与mybatis中字段的映射。如果前端传入的字段在map中不存在就使用前端传入的字段
     * @return 排序SQL字段
     */
    public static String getOrderString(Sort sort, Map<String, String> orderByFieldMap) {
        return sort.stream().map(t -> orderByFieldMap.getOrDefault(t.getProperty(), t.getProperty()) + ONE_SPACE + t.getDirection()).collect(Collectors.joining(","));
    }
}
