package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.core.domain.Page;

/**
 * Created by Sheep on 2019/7/18.
 */
public class MergeRequestTotalVO {

    private Long mergeCount;
    private Long openCount;
    private Long closeCount;
    private Long totalCount;
    @ApiModelProperty("待这个用户审核的merge request的数量")
    private Long auditCount;
    private Page<MergeRequestVO> mergeRequestVOPageInfo;


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

    public Page<MergeRequestVO> getMergeRequestVOPageInfo() {
        return mergeRequestVOPageInfo;
    }

    public void setMergeRequestVOPageInfo(Page<MergeRequestVO> mergeRequestVOPageInfo) {
        this.mergeRequestVOPageInfo = mergeRequestVOPageInfo;
    }

    public Long getAuditCount() {
        return auditCount;
    }

    public void setAuditCount(Long auditCount) {
        this.auditCount = auditCount;
    }
}
