package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/13 14:25
 */
public class AduitStatusChangeVO {
    @ApiModelProperty("审核状态是否变更")
    private Boolean auditStatusChanged;
    @ApiModelProperty("当前状态")
    private String currentStatus;
    @ApiModelProperty("会签")
    private Integer countersigned;
    @ApiModelProperty("审核人")
    private String auditUserName;


    public Boolean getAuditStatusChanged() {
        return auditStatusChanged;
    }

    public void setAuditStatusChanged(Boolean auditStatusChanged) {
        this.auditStatusChanged = auditStatusChanged;
    }

    public String getAuditUserName() {
        return auditUserName;
    }

    public void setAuditUserName(String auditUserName) {
        this.auditUserName = auditUserName;
    }

    public Integer getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Integer countersigned) {
        this.countersigned = countersigned;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}
