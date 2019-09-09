package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.dto.agile.IssueDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * Created by Sheep on 2019/7/18.
 */
public class BranchVO {


    private String branchName;
    private String sha;
    private String commitContent;
    private String commitUrl;
    private Date commitDate;
    private Date creationDate;
    private String commitUserUrl;
    private String commitUserName;
    private String createUserUrl;
    private String createUserName;
    private String createUserRealName;
    private Long issueId;
    private String issueCode;
    private String issueName;
    private String typeCode;
    private String status;
    private String errorMessage;

    @ApiModelProperty("分支纪录的版本号")
    private Long objectVersionNumber;

    public BranchVO() {
    }

    /**
     * construct
     */
    public BranchVO(DevopsBranchDTO devopsBranchDTO, String lastCommitUrl,
                    String createUserUrl,
                    IssueDTO issue,
                    IamUserDTO commitUserE, String createUserName, String realName, String status, String errorMessage) {
        this.branchName = devopsBranchDTO.getBranchName();
        this.sha = devopsBranchDTO.getLastCommit();
        this.commitContent = devopsBranchDTO.getLastCommitMsg();
        this.commitUserUrl = commitUserE.getImageUrl();
        this.creationDate = devopsBranchDTO.getCreationDate();
        this.commitUrl = lastCommitUrl;
        this.issueId = devopsBranchDTO.getIssueId();
        this.issueCode = issue == null ? null : issue.getIssueNum();
        this.issueName = issue == null ? null : issue.getSummary();
        this.commitDate = devopsBranchDTO.getLastCommitDate();
        this.createUserUrl = createUserUrl;
        this.typeCode = issue == null ? null : issue.getTypeCode();
        this.commitUserName = commitUserE.getRealName();
        this.createUserName = createUserName;
        this.createUserRealName = realName;
        this.status = status;
        this.errorMessage = errorMessage;
        this.objectVersionNumber = devopsBranchDTO.getObjectVersionNumber();
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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

    public String getCreateUserRealName() {
        return createUserRealName;
    }

    public void setCreateUserRealName(String createUserRealName) {
        this.createUserRealName = createUserRealName;
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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
