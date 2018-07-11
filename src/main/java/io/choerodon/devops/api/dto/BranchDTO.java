package io.choerodon.devops.api.dto;

import java.util.Date;

import io.choerodon.devops.infra.dataobject.DevopsBranchDO;

public class BranchDTO {
    private String name;
    private String sha;
    private String commitContent;
    private String commitUrl;
    private Date commitDate;
    private Date createDate;
    private String commitUserUrl;
    private String commitUserName;
    private String createUserUrl;
    private String createUserName;
    private Long issueId;
    private String issueCode;
    private String issueName;
    private String typeCode;

    public BranchDTO() {
    }

    /**
     * construct
     */
    public BranchDTO(DevopsBranchDO branchDO, String lastCommitUrl, Date createDate,
                     String createUserUrl, Long issueId,
                     String issueCode, String issueName,
                     String commitUserUrl, String typeCode,
                     String commitUserName, String createUserName) {
        this.name = branchDO.getBranchName();
        this.sha = branchDO.getLastCommit();
        this.commitContent = branchDO.getLastCommitMsg();
        this.commitUserUrl = commitUserUrl;
        this.createDate = createDate;
        this.commitUrl = lastCommitUrl;
        this.issueId = issueId;
        this.issueCode = issueCode;
        this.issueName = issueName;
        this.commitDate = branchDO.getLastCommitDate();
        this.createUserUrl = createUserUrl;
        this.typeCode = typeCode;
        this.commitUserName = commitUserName;
        this.createUserName = createUserName;
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

    public String getCommitContent() {
        return commitContent;
    }

    public void setCommitContent(String commitContent) {
        this.commitContent = commitContent;
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

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getCommitUserName() {
        return commitUserName;
    }

    public void setCommitUserName(String commitUserName) {
        this.commitUserName = commitUserName;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }
}
