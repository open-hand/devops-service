package io.choerodon.devops.infra.util;

import java.util.ArrayList;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zmf
 */
public class PageInfoUtil {
    private PageInfoUtil() {
    }

    /**
     * 把list转为分页查询的对象
     *
     * @param all 结果集
     * @param <T> 结果泛型
     * @return 分页结果
     */
    public static <T> Page<T> listAsPage(List<T> all) {
        Page<T> result = new Page<>();
        result.setContent(all);
        result.setTotalPages(GitOpsConstants.FIRST_PAGE_INDEX);
        result.setTotalElements(all.size());
        result.setNumber(1);
        result.setNumberOfElements(all.size());
        result.setSize(all.size());
        return result;
    }

    /**
     * 根据分页信息从所有的结果中设置Page对象
     *
     * @param all      包含所有内容的列表
     * @param pageable 分页参数
     * @return 根据分页参数所得分页内容
     */
    public static <T> Page<T> createPageFromList(List<T> all, PageRequest pageable) {
        Page<T> result = new Page<>();
        boolean queryAll = pageable.getSize() == 0;
        //当前页大小
        result.setNumberOfElements(queryAll ? all.size() : pageable.getSize());
        //当前页
        result.setNumber(pageable.getPage());
        //总共的大小
        result.setTotalElements(all.size());
        int ceilTotal = (int) (Math.ceil(all.size() / (pageable.getSize() * 1.0)));
        //总页数 从0开始
        result.setTotalPages(queryAll ? GitOpsConstants.FIRST_PAGE_INDEX : Math.max(0, ceilTotal - 1));
        //元素起始索引
        // 第一页从0开始
        int fromIndex = pageable.getSize() * pageable.getPage();
        int size;
        if (all.size() >= fromIndex) {
            if (all.size() <= fromIndex + pageable.getSize()) {
                size = all.size() - fromIndex;
            } else {
                size = pageable.getSize();
            }
            result.setSize(queryAll ? all.size() : size);
            result.setContent(queryAll ? all : all.subList(fromIndex, fromIndex + result.getSize()));
        } else {
            size = 0;
            result.setSize(queryAll ? all.size() : size);
            result.setContent(new ArrayList<>());
        }
        return result;
    }
}
