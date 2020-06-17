package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author lihao
 */
public class ApprovalVO {

    @ApiModelProperty("项目名称")
    private String organizationNameAndProjectName;

    @ApiModelProperty("用户头像")
    private String imageUrl;

    @ApiModelProperty("审批内容")
    private String content;

    @ApiModelProperty("审批消息类型")
    private String type;

    @ApiModelProperty("gitlab项目id")
    private Integer gitlabProjectId;

    @ApiModelProperty("pipeline id")
    private Long pipelineId;

    @ApiModelProperty("pipeline record id")
    private Long pipelineRecordId;

    @ApiModelProperty("pipeline stage id")
    private Long stageRecordId;

    @ApiModelProperty("pripeline task id")
    private Long taskRecordId;

    public String getOrganizationNameAndProjectName() {
        return organizationNameAndProjectName;
    }

    public ApprovalVO setOrganizationNameAndProjectName(String organizationNameAndProjectName) {
        this.organizationNameAndProjectName = organizationNameAndProjectName;
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

    @Override
    public String toString() {
        return "ApprovalVO{" +
                "organizationNameAndProjectName='" + organizationNameAndProjectName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", gitlabProjectId=" + gitlabProjectId +
                ", pipelineId=" + pipelineId +
                ", pipelineRecordId=" + pipelineRecordId +
                ", stageRecordId=" + stageRecordId +
                ", taskRecordId=" + taskRecordId +
                '}';
    }
}
