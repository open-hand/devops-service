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

    @ApiModelProperty(hidden = true)
    List<IssueInfo> issueInfoList;
    @ApiModelProperty("分支名")
    private String branchName;
    @ApiModelProperty("分支名")
    private String sha;
    @ApiModelProperty("提交内容")
    private String commitContent;
    @ApiModelProperty("提交地址")
    private String commitUrl;
    @ApiModelProperty("提交日期")
    private Date commitDate;
    @ApiModelProperty("创建日期")
    private Date creationDate;
    @ApiModelProperty("提交人头像")
    private String commitUserUrl;
    @ApiModelProperty("提交人")
    private String commitUserName;
    @ApiModelProperty("提交人真实姓名")
    private String commitUserRealName;
    @ApiModelProperty(hidden = true)
    private String createUserUrl;
    @ApiModelProperty(hidden = true)
    private String createUserName;
    @ApiModelProperty(hidden = true)
    private String createUserRealName;
    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("分支纪录的版本号")
    private Long objectVersionNumber;
    @ApiModelProperty("错误信息")
    private String errorMessage;
    @Encrypt
    @ApiModelProperty("saga实例id")
    private Long sagaInstanceId;

    public BranchVO() {
    }

    /**
     * construct
     */
    public  BranchVO(DevopsBranchDTO devopsBranchDTO, String lastCommitUrl,
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
        this.commitUserName = commitUserE == null ? null : commitUserE.getLdap() ? commitUserE.getLoginName() : commitUserE.getEmail();
        this.commitUserRealName = commitUserE == null ? null : commitUserE.getRealName();
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
                issueInfo.setIssueId(i.getIssueId());
                issueInfo.setIssueCode(i.getProjectCode() + "-" + i.getIssueNum());
                issueInfo.setIssueName(i.getSummary());
                issueInfo.setProjectName(i.getProjectName());
                issueInfo.setTypeCode(i.getTypeCode());
                issueInfo.setIssueProjectId(i.getProjectId());
                issueInfoList.add(issueInfo);
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

    public List<IssueInfo> getIssueInfoList() {
        return issueInfoList;
    }

    public void setIssueInfoList(List<IssueInfo> issueInfoList) {
        this.issueInfoList = issueInfoList;
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
