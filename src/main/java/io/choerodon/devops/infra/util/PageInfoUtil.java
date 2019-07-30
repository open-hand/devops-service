package io.choerodon.devops.infra.util;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;

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
     * @param pageRequest 分页参数
     * @return 根据分页参数所得分页内容
     */
    public static <T> PageInfo<T> createPageFromList(List<T> all, PageRequest pageRequest) {
        PageInfo<T> result = new PageInfo<>();
        result.setPageSize(pageRequest.getSize());
        result.setPageNum(pageRequest.getPage());
        result.setTotal(all.size());
        result.setPages((int) (Math.ceil(all.size() / (pageRequest.getSize() * 1.0))));
        int fromIndex = pageRequest.getSize() * (pageRequest.getPage() - 1);
        result.setSize(all.size() < fromIndex ? (all.size() - fromIndex) : pageRequest.getSize());
        result.setList(all.subList(fromIndex, result.getSize()));
        return result;
    }
}
