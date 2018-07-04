package io.choerodon.devops.domain.application.entity;

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
    private Date lastCommitDate;
    private Date creationDate;
    private String commit;

    public DevopsBranchE() {
    }

    public DevopsBranchE(String commit, String branchName, ApplicationE applicationE, Date lastCommitDate) {
        this.commit = commit;
        this.branchName = branchName;
        this.applicationE = applicationE;
        this.lastCommitDate = lastCommitDate;
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

    public Date getLastCommitDate() {
        return lastCommitDate;
    }

    public void setLastCommitDate(Date lastCommitDate) {
        this.lastCommitDate = lastCommitDate;
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

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }
}

