package io.choerodon.devops.infra.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.collect.Lists;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsUserPermissionVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:58 2019/4/4
 * Description:
 */
public class PageRequestUtil {
    private static final String ONE_SPACE = " ";

    private PageRequestUtil() {
    }

    public static String checkSortIsEmpty(PageRequest pageable) {
        String index = "";
        if (pageable.getSort() == null) {
            index = "true";
        }
        return index;
    }


    public static String getOrderBy(PageRequest pageable) {
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
     * @param sort            {@link PageRequest#getSort()}中的sort对象
     * @param orderByFieldMap 前端传入的字段与mybatis中字段的映射。如果前端传入的字段在map中不存在就抛异常，防止SQL注入
     * @return 排序SQL字段
     */
    public static String getOrderString(Sort sort, Map<String, String> orderByFieldMap) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(sort.iterator(), Spliterator.ORDERED), false).map(t -> {
            String field = orderByFieldMap.get(t.getProperty());
            if (field == null) {
                throw new CommonException("error.field.not.supported.for.sort", t.getProperty());
            }
            return field + ONE_SPACE + t.getDirection();
        }).collect(Collectors.joining(","));
    }

    public static List<DevopsUserPermissionVO> sortUserPermission(List<DevopsUserPermissionVO> toBeSorted, Sort sort) {
        if (sort != null) {
            // 取第一个
            Sort.Order order = sort.iterator().next();
            switch (order.getProperty()) {
                case "loginName":
                    return PageRequestUtil.sortByComparableKey(toBeSorted, DevopsUserPermissionVO::getLoginName, order.getDirection());
                case "realName":
                    return PageRequestUtil.sortByComparableKey(toBeSorted, DevopsUserPermissionVO::getRealName, order.getDirection());
                case "creationDate":
                    return PageRequestUtil.sortByComparableKey(toBeSorted, DevopsUserPermissionVO::getCreationDate, order.getDirection());
                default:
                    throw new CommonException("error.field.not.supported.for.sort", order.getProperty());
            }
        } else {
            return toBeSorted;
        }
    }

    /**
     * 根据Comparable类型的属性对列表进行排序
     *
     * @param toBeSorted   待排序的列表
     * @param keyExtractor 提取Comparable类型属性的逻辑
     * @param direction    排序的升序或者降序
     * @param <T>          待排序对象类型
     * @param <U>          待排序对象用于排序的字段类型
     * @return 排序完成的列表
     */
    public static <T, U extends Comparable<? super U>> List<T> sortByComparableKey(List<T> toBeSorted, Function<? super T, ? extends U> keyExtractor, Sort.Direction direction) {
        Comparator<T> comparator = Comparator.comparing(keyExtractor);
        if (direction == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }
        toBeSorted.sort(comparator);
        return toBeSorted;
    }
}
