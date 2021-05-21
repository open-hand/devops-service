package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.util.CollectionUtils;

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
    private String commitUserRealName;
    private String createUserUrl;
    private String createUserName;
    private String createUserRealName;

    private String status;
    private String errorMessage;

    @ApiModelProperty("分支纪录的版本号")
    private Long objectVersionNumber;
    @Encrypt
    private Long sagaInstanceId;

    List<IssueInfo> issueInfoList;

    public BranchVO() {
    }

    /**
     * construct
     */
    public BranchVO(DevopsBranchDTO devopsBranchDTO, String lastCommitUrl,
                    String createUserUrl,
                    List<IssueDTO> issue,
                    IamUserDTO commitUserE, String createUserName, String realName, String status, String errorMessage, Long sagaInstanceId) {
        this.branchName = devopsBranchDTO.getBranchName();
        this.sha = devopsBranchDTO.getLastCommit();
        this.commitContent = devopsBranchDTO.getLastCommitMsg();
        this.commitUserUrl = commitUserE == null ? null : commitUserE.getImageUrl();
        this.creationDate = devopsBranchDTO.getCreationDate();
        this.commitUrl = lastCommitUrl;
        this.commitDate = devopsBranchDTO.getLastCommitDate();
        this.createUserUrl = createUserUrl;
        this.commitUserName = commitUserE == null ? null : commitUserE.getRealName();
        this.commitUserRealName = commitUserE == null ? null : commitUserE.getLdap() ? commitUserE.getLoginName() : commitUserE.getEmail();
        this.createUserName = createUserName;
        this.createUserRealName = realName;
        this.status = status;
        this.errorMessage = errorMessage;
        this.objectVersionNumber = devopsBranchDTO.getObjectVersionNumber();
        this.sagaInstanceId = sagaInstanceId;

        if (!CollectionUtils.isEmpty(issue)) {
            List<IssueInfo> issueInfoList = new ArrayList<>();
            issue.forEach(i -> {
                IssueInfo issueInfo = new IssueInfo();
                issueInfo.setIssueId(devopsBranchDTO.getIssueId());
                issueInfo.setIssueCode(i.getProjectCode() + "-" + i.getIssueNum());
                issueInfo.setIssueName(i.getSummary());
                issueInfo.setProjectName(i.getProjectName());
                issueInfo.setTypeCode(i.getTypeCode());
            });
            this.issueInfoList = issueInfoList;
        }
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

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
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

    public String getCommitUserRealName() {
        return commitUserRealName;
    }

    public void setCommitUserRealName(String commitUserRealName) {
        this.commitUserRealName = commitUserRealName;
    }

    public Long getSagaInstanceId() {
        return sagaInstanceId;
    }

    public void setSagaInstanceId(Long sagaInstanceId) {
        this.sagaInstanceId = sagaInstanceId;
    }

    public static class IssueInfo {
        @Encrypt
        private Long issueId;
        private String issueCode;
        private String issueName;
        private Long issueProjectId;
        private String typeCode;
        /**
         * 问题来源
         */
        private String projectName;

        public Long getIssueId() {
            return issueId;
        }

        public void setIssueId(Long issueId) {
            this.issueId = issueId;
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

        public Long getIssueProjectId() {
            return issueProjectId;
        }

        public void setIssueProjectId(Long issueProjectId) {
            this.issueProjectId = issueProjectId;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getTypeCode() {
            return typeCode;
        }

        public void setTypeCode(String typeCode) {
            this.typeCode = typeCode;
        }
    }
}
