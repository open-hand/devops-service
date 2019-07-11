package io.choerodon.devops.api.vo.iam.entity;

import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DevopsBranchE {

    private Long id;
    private ApplicationE applicationE;
    private Long userId;
    private Long issueId;
    private String branchName;
    private String originBranch;
    private Date checkoutDate;
    private Date creationDate;
    private String checkoutCommit;
    private Boolean isDeleted;
    private String lastCommit;
    private String lastCommitMsg;
    private Long lastCommitUser;
    private Date lastCommitDate;
    private String status;
    private String errorMessage;

    public DevopsBranchE() {
    }

    /**
     * Construct
     *
     * @param checkoutCommit 提交
     * @param branchName     分支名
     * @param applicationE   应用
     * @param checkoutDate   最新提交时间
     */
    public DevopsBranchE(String checkoutCommit, String branchName, ApplicationE applicationE, Date checkoutDate) {
        this.checkoutCommit = checkoutCommit;
        this.branchName = branchName;
        this.applicationE = applicationE;
        this.checkoutDate = checkoutDate;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(Date checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public ApplicationE getApplicationE() {
        return applicationE;
    }

    public void setApplicationE(ApplicationE applicationE) {
        this.applicationE = applicationE;
    }

    public void initApplicationE(Long id) {
        this.applicationE = new ApplicationE(id);
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getOriginBranch() {
        return originBranch;
    }

    public void setOriginBranch(String originBranch) {
        this.originBranch = originBranch;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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

