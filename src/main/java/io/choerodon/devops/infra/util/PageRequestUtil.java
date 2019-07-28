package io.choerodon.devops.infra.util;

import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:58 2019/4/4
 * Description:
 */
public class PageRequestUtil {
    private PageRequestUtil() {
    }

    public static String checkSortIsEmpty(PageRequest pageRequest) {
        String index = "";
        if (pageRequest.getSort() == null) {
            index = "true";
        }
        return index;
    }


    public static String getOrderBy(PageRequest pageRequest) {
        Sort sort = pageRequest.getSort();
        if (sort != null) {
            return Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> t.getProperty() + " " + t.getDirection())
                    .collect(Collectors.joining(","));
        }
        return "";
    }

    public static String getOrderByStr(PageRequest pageRequest) {
        Sort sort = pageRequest.getSort();
        if (sort != null) {
            return Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> t.getProperty() + "," + t.getDirection())
                    .collect(Collectors.joining(","));
        }
        return "";
    }
}
