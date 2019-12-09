package io.choerodon.devops.infra.util;

import com.github.pagehelper.PageInfo;
import io.choerodon.mybatis.autoconfigure.CustomPageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zmf
 */
public class PageInfoUtil {
    private PageInfoUtil() {
    }

    /**
     * 根据分页信息从所有的结果中设置Page对象
     *
     * @param all      包含所有内容的列表
     * @param pageable 分页参数
     * @return 根据分页参数所得分页内容
     */
    public static <T> PageInfo<T> createPageFromList(List<T> all, Pageable pageable) {
        PageInfo<T> result = new PageInfo<>();
        boolean queryAll = pageable.getPageNumber() == 0 || pageable.getPageSize() == 0;
        //当前页大小
        result.setPageSize(queryAll ? all.size() : pageable.getPageSize());
        //当前页
        result.setPageNum(pageable.getPageNumber());
        //总共的大小
        result.setTotal(all.size());
        //总页数
        result.setPages(queryAll ? 1 : (int) (Math.ceil(all.size() / (pageable.getPageSize() * 1.0))));
        //元素起始索引
        int fromIndex = pageable.getPageSize() * (pageable.getPageNumber() - 1);
        int size;
        if (all.size() >= fromIndex) {
            if (all.size() <= fromIndex + pageable.getPageSize()) {
                size = all.size() - fromIndex;
            } else {
                size = pageable.getPageSize();
            }
            // 页数小于等于1
            if (result.getPages() <= 1) {
                result.setHasPreviousPage(false);
                result.setHasNextPage(false);
                result.setIsFirstPage(true);
                result.setIsLastPage(true);
            } else {
                // 当前页为1，且总页数大于1
                if (pageable.getPageNumber() == 1) {
                    result.setHasPreviousPage(false);
                    result.setHasNextPage(true);
                    result.setIsFirstPage(true);
                    result.setIsLastPage(false);
                }
                // 当前页大于第一页且小于总页数
                if (1 < pageable.getPageNumber() && pageable.getPageNumber() < result.getPages()) {
                    result.setHasPreviousPage(true);
                    result.setHasNextPage(true);
                    result.setIsFirstPage(false);
                    result.setIsLastPage(false);
                }
                // 当前页等于最后一页且总页数大于1
                if (pageable.getPageNumber() == result.getPages()) {
                    result.setHasPreviousPage(true);
                    result.setHasNextPage(false);
                    result.setIsFirstPage(false);
                    result.setIsLastPage(true);
                }
            }
            result.setSize(queryAll ? all.size() : size);
            result.setList(queryAll ? all : all.subList(fromIndex, fromIndex + result.getSize()));
        } else {
            size = 0;
            result.setSize(queryAll ? all.size() : size);
            result.setList(new ArrayList<>());
        }
        return result;
    }
}
