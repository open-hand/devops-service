package io.choerodon.devops.infra.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

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
    private PageRequestUtil() {
    }

    public static String checkSortIsEmpty(PageRequest pageable) {
        String index = "";
        if (pageable.getSort() == null) {
            index = "true";
        }
        return index;
    }

    /**
     * 简单的将page对象中的排序字段转换为下划线形式
     *
     * @param pageRequest 分页请求
     * @return 转化过sort的分页请求
     */
    public static PageRequest simpleConvertSortForPage(PageRequest pageRequest) {
        pageRequest.setSort(simpleConvertSort(pageRequest.getSort()));
        return pageRequest;
    }

    /**
     * 简单的sort进行处理转换
     *
     * @param sort 排序字段，可为空
     * @return 返回转为下划线形式的排序字段
     */
    @Nullable
    public static Sort simpleConvertSort(@Nullable Sort sort) {
        if (sort != null) {
            List<Sort.Order> orders = new ArrayList<>();
            sort.iterator().forEachRemaining(s -> orders.add(new Sort.Order(s.getDirection(), HumpToUnderlineUtil.toUnderLine(s.getProperty()))));
            return new Sort(orders);
        } else {
            return null;
        }
    }

    public static String getOrderBy(Sort sort) {
        if (sort != null) {
            return Lists.newArrayList(sort.iterator()).stream()
                    .map(t -> t.getProperty() + " " + t.getDirection())
                    .collect(Collectors.joining(","));
        }
        return "";
    }

    public static String getOrderBy(PageRequest pageRequest) {
        return getOrderBy(pageRequest.getSort());
    }

    /**
     * 处理排序字段
     *
     * @param page            page请求
     * @param orderByFieldMap 前端传入的字段与mybatis中字段的映射。如果前端传入的字段在map中不存在就抛异常，防止SQL注入
     * @return 排序SQL字段
     */
    public static PageRequest getMappedPage(PageRequest page, Map<String, String> orderByFieldMap) {
        if (page.getSort() != null) {
            page.setSort(getMappedSort(page.getSort(), orderByFieldMap));
        }
        return page;
    }

    /**
     * 处理排序字段
     *
     * @param sort            排序数据
     * @param orderByFieldMap 前端传入的字段与mybatis中字段的映射。如果前端传入的字段在map中不存在就抛异常，防止SQL注入
     * @return 排序SQL字段
     */
    public static Sort getMappedSort(Sort sort, Map<String, String> orderByFieldMap) {
        List<Sort.Order> newOrders = new ArrayList<>();
        sort.iterator().forEachRemaining(s -> {
            String field = orderByFieldMap.get(s.getProperty());
            if (field == null) {
                throw new CommonException("error.field.not.supported.for.sort", s.getProperty());
            }
            newOrders.add(new Sort.Order(s.getDirection(), field));
        });
        return new Sort(newOrders);
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
