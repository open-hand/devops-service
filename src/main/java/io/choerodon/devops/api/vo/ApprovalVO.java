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

    @ApiModelProperty("pipeline record id")
    private Long pipeRecordId;

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

    public Long getPipeRecordId() {
        return pipeRecordId;
    }

    public ApprovalVO setPipeRecordId(Long pipeRecordId) {
        this.pipeRecordId = pipeRecordId;
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
                ", pipeRecordId=" + pipeRecordId +
                '}';
    }
}
