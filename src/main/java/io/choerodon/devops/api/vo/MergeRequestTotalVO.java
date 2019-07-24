package io.choerodon.devops.api.vo;

import com.github.pagehelper.PageInfo;

/**
 * Created by Sheep on 2019/7/18.
 */
public class MergeRequestTotalVO {

    private Long mergeCount;
    private Long openCount;
    private Long closeCount;
    private Long totalCount;
    private PageInfo<MergeRequestVO> mergeRequestVOPageInfo;


    public Long getMergeCount() {
        return mergeCount;
    }

    public void setMergeCount(Long mergeCount) {
        this.mergeCount = mergeCount;
    }

    public Long getOpenCount() {
        return openCount;
    }

    public void setOpenCount(Long openCount) {
        this.openCount = openCount;
    }

    public Long getCloseCount() {
        return closeCount;
    }

    public void setCloseCount(Long closeCount) {
        this.closeCount = closeCount;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public PageInfo<MergeRequestVO> getMergeRequestVOPageInfo() {
        return mergeRequestVOPageInfo;
    }

    public void setMergeRequestVOPageInfo(PageInfo<MergeRequestVO> mergeRequestVOPageInfo) {
        this.mergeRequestVOPageInfo = mergeRequestVOPageInfo;
    }
}
