package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;

public class DevopsBranchVO {
    @ApiModelProperty("应用服务id")
    @Encrypt
    private Long appServiceId;

    @ApiModelProperty("应用服务code")
    private String appServiceCode;

    @ApiModelProperty("应用服务名称")
    private String appServiceName;

    @ApiModelProperty("源分支，此分支基于源分支创建")
    private String originBranch;

    @Encrypt
    @ApiModelProperty("关联的敏捷Issue的id")
    private Long issueId;

    @ApiModelProperty("分支名")
    private String branchName;

    @ApiModelProperty("commits")
    private List<DevopsGitlabCommitDTO> commits;

    private List<CustomMergeRequestVO> mergeRequests;

    @ApiModelProperty("分支纪录的版本号")
    private Long objectVersionNumber;

    @ApiModelProperty("所属项目id")
    private Long projectId;

    @ApiModelProperty("所属项目名称")
    private String projectName;

    @ApiModelProperty("所属项目code")
    private String projectCode;

    @Encrypt
    @ApiModelProperty("分支id")
    private Long id;

    public String getOriginBranch() {
        return originBranch;
    }

    public void setOriginBranch(String originBranch) {
        this.originBranch = originBranch;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public List<DevopsGitlabCommitDTO> getCommits() {
        return commits;
    }

    public void setCommits(List<DevopsGitlabCommitDTO> commits) {
        this.commits = commits;
    }

    public List<CustomMergeRequestVO> getMergeRequests() {
        return mergeRequests;
    }

    public void setMergeRequests(List<CustomMergeRequestVO> mergeRequests) {
        this.mergeRequests = mergeRequests;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }
}
