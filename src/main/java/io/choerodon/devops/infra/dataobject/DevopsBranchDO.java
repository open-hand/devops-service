package io.choerodon.devops.infra.dataobject;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

@VersionAudit
@ModifyAudit
@Table(name = "devops_branch")
public class DevopsBranchDO {

    @Id
    @GeneratedValue
    private Long id;
    private Long appId;
    private Long userId;
    private Long issueId;
    private String originBranch;
    private String branchName;
    private Date checkoutDate;
    private String checkoutCommit;
    private Boolean isDeleted;
    private Date creationDate;
    private Long objectVersionNumber;
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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
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
}
