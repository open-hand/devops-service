package io.choerodon.devops.infra.util;

import java.util.ArrayList;
import java.util.List;

import com.github.pagehelper.PageInfo;

import org.springframework.data.domain.Pageable;

/**
 * @author zmf
 */
public class PageInfoUtil {
    private PageInfoUtil() {
    }

    /**
     * 根据分页信息从所有的结果中设置Page对象
     *
     * @param all         包含所有内容的列表
     * @param pageable 分页参数
     * @return 根据分页参数所得分页内容
     */
    public static <T> PageInfo<T> createPageFromList(List<T> all, Pageable pageable) {
        PageInfo<T> result = new PageInfo<>();
        boolean queryAll = pageable.getPageNumber() == 0 || pageable.getPageSize() == 0;
        result.setPageSize(queryAll ? all.size() : pageable.getPageSize());
        result.setPageNum(pageable.getPageNumber());
        result.setTotal(all.size());
        result.setPages(queryAll ? 1 : (int) (Math.ceil(all.size() / (pageable.getPageSize() * 1.0))));
        int fromIndex = pageable.getPageSize() * (pageable.getPageNumber() - 1);
        int size;
        if (all.size() >= fromIndex) {
            if (all.size() <= fromIndex + pageable.getPageSize()) {
                size = all.size() - fromIndex;
            } else {
                size = pageable.getPageSize();
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
