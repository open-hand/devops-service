package io.choerodon.devops.infra.dto;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.entity.BaseDTO;

@Table(name = "devops_branch")
public class DevopsBranchDTO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long appServiceId;
    private Long userId;
    private Long issueId;
    private String originBranch;
    private String status;
    private String errorMessage;
    private String branchName;
    private Date checkoutDate;
    private String checkoutCommit;
    private Boolean isDeleted;
    private String lastCommit;
    private String lastCommitMsg;
    private Long lastCommitUser;
    private Date lastCommitDate;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getOriginBranch() {
        return originBranch;
    }

    public void setOriginBranch(String originBranch) {
        this.originBranch = originBranch;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Date getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(Date checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public String getCheckoutCommit() {
        return checkoutCommit;
    }

    public void setCheckoutCommit(String checkoutCommit) {
        this.checkoutCommit = checkoutCommit;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public String getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(String lastCommit) {
        this.lastCommit = lastCommit;
    }

    public String getLastCommitMsg() {
        return lastCommitMsg;
    }

    public void setLastCommitMsg(String lastCommitMsg) {
        this.lastCommitMsg = lastCommitMsg;
    }

    public Long getLastCommitUser() {
        return lastCommitUser;
    }

    public void setLastCommitUser(Long lastCommitUser) {
        this.lastCommitUser = lastCommitUser;
    }

    public Date getLastCommitDate() {
        return lastCommitDate;
    }

    public void setLastCommitDate(Date lastCommitDate) {
        this.lastCommitDate = lastCommitDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
