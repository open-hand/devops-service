package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.iam.UserVO;

/**
 * GitLabDTO 合并请求
 */
public class MergeRequestVO {

    @ApiModelProperty("审核人")
    private AssigneeVO assignee;
    @Encrypt
    @ApiModelProperty("审核人id")
    private Long assigneeId;
    @ApiModelProperty("提交人")
    private AuthorVO author;
    @Encrypt
    @ApiModelProperty("提交人id")
    private Long authorId;
    @ApiModelProperty("创建日期")
    private Date createdAt;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("gitlab mr id")
    private Integer iid;
    @ApiModelProperty("合并状态")
    private String mergeStatus;
    @ApiModelProperty("gitlab project id")
    private Integer projectId;
    @ApiModelProperty("源分支")
    private String sourceBranch;
    @ApiModelProperty(hidden = true)
    private Integer sourceProjectId;
    @ApiModelProperty("合并状态")
    private String state;
    @ApiModelProperty("目标分支")
    private String targetBranch;
    @ApiModelProperty(hidden = true)
    private Integer targetProjectId;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("更新日期")
    private Date updatedAt;
    @ApiModelProperty("gitlab url")
    private String webUrl;
    @ApiModelProperty("gitlab mr id")
    private String gitlabMergeRequestId;

    @Encrypt
    @ApiModelProperty("应用服务 id")
    private Long appServiceId;
    @ApiModelProperty("应用服务名")
    private String appServiceName;
    @ApiModelProperty("应用服务编码")
    private String appServiceCode;
    @ApiModelProperty("gitlab url")
    private String gitlabUrl;
    @ApiModelProperty("iam作者信息")
    private UserVO iamAuthor;
    @ApiModelProperty("iam审核人信息")
    private UserVO iamAssignee;

    @ApiModelProperty(value = "提交记录", hidden = true)
    private List<CommitVO> commits;

    public static Boolean isValid(MergeRequestVO mergeRequestVO) {
        return (mergeRequestVO != null && mergeRequestVO.getId() != null);
    }

    public AssigneeVO getAssignee() {
        return assignee;
    }

    public void setAssignee(AssigneeVO assignee) {
        this.assignee = assignee;
    }

    public AuthorVO getAuthor() {
        return author;
    }

    public void setAuthor(AuthorVO author) {
        this.author = author;
    }

    public List<CommitVO> getCommits() {
        return commits;
    }

    public void setCommits(List<CommitVO> commits) {
        this.commits = commits;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIid() {
        return iid;
    }

    public void setIid(Integer iid) {
        this.iid = iid;
    }

    public String getMergeStatus() {
        return mergeStatus;
    }

    public void setMergeStatus(String mergeStatus) {
        this.mergeStatus = mergeStatus;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public Integer getSourceProjectId() {
        return sourceProjectId;
    }

    public void setSourceProjectId(Integer sourceProjectId) {
        this.sourceProjectId = sourceProjectId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public Integer getTargetProjectId() {
        return targetProjectId;
    }

    public void setTargetProjectId(Integer targetProjectId) {
        this.targetProjectId = targetProjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public UserVO getIamAuthor() {
        return iamAuthor;
    }

    public void setIamAuthor(UserVO iamAuthor) {
        this.iamAuthor = iamAuthor;
    }

    public UserVO getIamAssignee() {
        return iamAssignee;
    }

    public void setIamAssignee(UserVO iamAssignee) {
        this.iamAssignee = iamAssignee;
    }

    public String getGitlabMergeRequestId() {
        return gitlabMergeRequestId;
    }

    public void setGitlabMergeRequestId(String gitlabMergeRequestId) {
        this.gitlabMergeRequestId = gitlabMergeRequestId;
    }
}
