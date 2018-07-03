package io.choerodon.devops.api.dto;

import java.util.Date;

import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;

public class BranchDTO {
    private String name;
    private String sha;
    private String commitComent;
    private String commitUrl;
    private Date createDate;
    private String commitUserUrl;
    private String createUserUrl;
    private Long issueId;
    private String issueCode;
    private String issueName;

    public BranchDTO(){}

    public BranchDTO(BranchDO branchDO, Date createDate, String createUserUrl, Long issueId, String issueCode, String issueName, String commitUserUrl) {
        this.name = branchDO.getName();
        this.sha = branchDO.getCommit().getShortId();
        this.commitComent = branchDO.getCommit().getMessage();
        this.commitUserUrl = commitUserUrl;
        this.createDate = createDate;
        this.commitUrl = branchDO.getCommit().getUrl();
        this.issueId = issueId;
        this.issueCode = issueCode;
        this.issueName = issueName;
        this.createUserUrl = createUserUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getCommitComent() {
        return commitComent;
    }

    public void setCommitComent(String commitComent) {
        this.commitComent = commitComent;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public String getIssueCode() {
        return issueCode;
    }

    public void setIssueCode(String issueCode) {
        this.issueCode = issueCode;
    }

    public String getIssueName() {
        return issueName;
    }

    public void setIssueName(String issueName) {
        this.issueName = issueName;
    }

    public String getCommitUserUrl() {
        return commitUserUrl;
    }

    public void setCommitUserUrl(String commitUserUrl) {
        this.commitUserUrl = commitUserUrl;
    }

    public String getCreateUserUrl() {
        return createUserUrl;
    }

    public void setCreateUserUrl(String createUserUrl) {
        this.createUserUrl = createUserUrl;
    }

}
