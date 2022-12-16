package io.choerodon.devops.api.vo.pipeline;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

public class Audit {
    private List<IamUserDTO> appointUsers;
    private List<IamUserDTO> reviewedUsers;
    private String status;
    @ApiModelProperty(value = "是否会签 1是会签,0 是或签", required = true)
    private Boolean countersigned;

    @ApiModelProperty(value = "当前用户是否拥有审核权限")
    private Boolean canAuditFlag;

    public Audit() {
    }


    public Audit(List<IamUserDTO> appointUsers, List<IamUserDTO> reviewedUsers, String status, Boolean countersigned) {
        this.appointUsers = appointUsers;
        this.reviewedUsers = reviewedUsers;
        this.status = status;
        this.countersigned = countersigned;
    }

    public Boolean getCanAuditFlag() {
        return canAuditFlag;
    }

    public void setCanAuditFlag(Boolean canAuditFlag) {
        this.canAuditFlag = canAuditFlag;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }

    public List<IamUserDTO> getAppointUsers() {
        return appointUsers;
    }

    public void setAppointUsers(List<IamUserDTO> appointUsers) {
        this.appointUsers = appointUsers;
    }

    public List<IamUserDTO> getReviewedUsers() {
        return reviewedUsers;
    }

    public void setReviewedUsers(List<IamUserDTO> reviewedUsers) {
        this.reviewedUsers = reviewedUsers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}