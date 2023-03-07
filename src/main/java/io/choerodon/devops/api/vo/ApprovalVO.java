package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author lihao
 */
public class ApprovalVO {

    @ApiModelProperty("项目名称")
    private String projectName;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("用户头像")
    private String imageUrl;
    @ApiModelProperty("审批内容")
    private String content;
    @ApiModelProperty("审批消息类型")
    private String type;
    @ApiModelProperty("gitlab项目id")
    private Integer gitlabProjectId;
    @ApiModelProperty("合并请求gitlab项目地址")
    private String mergeRequestUrl;
    @ApiModelProperty("pipeline id")
    private Long pipelineId;
    @ApiModelProperty("cd pipeline id")
    private Long cdPipelineId;
    @ApiModelProperty("pipeline record id")
    private Long pipelineRecordId;
    @ApiModelProperty("Cd pipeline record id")
    private Long cdPipelineRecordId;
    @ApiModelProperty("ci 和 cd 关联关系id")
    private Long devopsPipelineRecordRelId;
    @Encrypt
    @ApiModelProperty("pipeline stage id")
    private Long stageRecordId;
    @Encrypt
    @ApiModelProperty("pipeline task id")
    private Long taskRecordId;

    private String stageName;

    private String pipelineName;

    public Long getCdPipelineId() {
        return cdPipelineId;
    }

    public void setCdPipelineId(Long cdPipelineId) {
        this.cdPipelineId = cdPipelineId;
    }

    public Long getCdPipelineRecordId() {
        return cdPipelineRecordId;
    }

    public void setCdPipelineRecordId(Long cdPipelineRecordId) {
        this.cdPipelineRecordId = cdPipelineRecordId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getProjectName() {
        return projectName;
    }

    public ApprovalVO setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public Long getProjectId() {
        return projectId;
    }

    public ApprovalVO setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ApprovalVO setContent(String content) {
        this.content = content;
        return this;
    }

    public String getType() {
        return type;
    }

    public ApprovalVO setType(String type) {
        this.type = type;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ApprovalVO setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Integer getGitlabProjectId() {
        return gitlabProjectId;
    }

    public ApprovalVO setGitlabProjectId(Integer gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
        return this;
    }

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public ApprovalVO setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
        return this;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public ApprovalVO setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
        return this;
    }

    public Long getTaskRecordId() {
        return taskRecordId;
    }

    public ApprovalVO setTaskRecordId(Long taskRecordId) {
        this.taskRecordId = taskRecordId;
        return this;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public ApprovalVO setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
        return this;
    }

    public String getMergeRequestUrl() {
        return mergeRequestUrl;
    }

    public ApprovalVO setMergeRequestUrl(String mergeRequestUrl) {
        this.mergeRequestUrl = mergeRequestUrl;
        return this;
    }

    public Long getDevopsPipelineRecordRelId() {
        return devopsPipelineRecordRelId;
    }

    public ApprovalVO setDevopsPipelineRecordRelId(Long devopsPipelineRecordRelId) {
        this.devopsPipelineRecordRelId = devopsPipelineRecordRelId;
        return this;
    }

    @Override
    public String toString() {
        return "ApprovalVO{" +
                "projectName='" + projectName + '\'' +
                ", projectId=" + projectId +
                ", imageUrl='" + imageUrl + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", gitlabProjectId=" + gitlabProjectId +
                ", mergeRequestUrl='" + mergeRequestUrl + '\'' +
                ", pipelineId=" + pipelineId +
                ", pipelineRecordId=" + pipelineRecordId +
                ", devopsPipelineRecordRelId=" + devopsPipelineRecordRelId +
                ", stageRecordId=" + stageRecordId +
                ", taskRecordId=" + taskRecordId +
                '}';
    }
}
