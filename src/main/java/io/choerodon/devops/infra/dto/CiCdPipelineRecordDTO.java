package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "cicd_pipeline_record")
public class CiCdPipelineRecordDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty(name = "流水线id")
    private Long cicdPipelineId;
    @ApiModelProperty(name = "流水线名字")
    private String pipelineName;
    @ApiModelProperty(name = "gitlab流水线记录id")
    private String gitlabPipelineId;
    @ApiModelProperty(name = "commit_sha")
    private String commitSha;
    @ApiModelProperty(name = "触发分支")
    private String gitlabTriggerRef;
    @ApiModelProperty(name = "流水线状态")
    private String status;
    @ApiModelProperty(name = "gitlab_project_id")
    private Long gitlabProjectId;
    @ApiModelProperty(name = "触发用户id")
    private Long triggerUserId;
    @ApiModelProperty(name = "gitlab流水线开始时间")
    private Date createdDate;
    @ApiModelProperty(name = "gitlab流水线结束时间")
    private Date finishedDate;
    @ApiModelProperty(name = "触发方式")
    private String triggerType;
    @ApiModelProperty(name = "bpm定义")
    private String bpmDefinition;
    @ApiModelProperty(name = "流程实例")
    private String businessKey;
    @ApiModelProperty(name = "是否编辑")
    private Boolean edited;
    @ApiModelProperty(name = "审核人员")
    private String auditUser;
    @ApiModelProperty(name = "错误信息")
    private String errorInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCicdPipelineId() {
        return cicdPipelineId;
    }

    public void setCicdPipelineId(Long cicdPipelineId) {
        this.cicdPipelineId = cicdPipelineId;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(String gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getGitlabTriggerRef() {
        return gitlabTriggerRef;
    }

    public void setGitlabTriggerRef(String gitlabTriggerRef) {
        this.gitlabTriggerRef = gitlabTriggerRef;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public Long getTriggerUserId() {
        return triggerUserId;
    }

    public void setTriggerUserId(Long triggerUserId) {
        this.triggerUserId = triggerUserId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getBpmDefinition() {
        return bpmDefinition;
    }

    public void setBpmDefinition(String bpmDefinition) {
        this.bpmDefinition = bpmDefinition;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Boolean getEdited() {
        return edited;
    }

    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    public String getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(String auditUser) {
        this.auditUser = auditUser;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    @Override
    public String toString() {
        return "CiCdPipelineRecordDTO{" +
                "id=" + id +
                ", cicdPipelineId=" + cicdPipelineId +
                ", pipelineName='" + pipelineName + '\'' +
                ", gitlabPipelineId='" + gitlabPipelineId + '\'' +
                ", commitSha='" + commitSha + '\'' +
                ", gitlabTriggerRef='" + gitlabTriggerRef + '\'' +
                ", status='" + status + '\'' +
                ", gitlabProjectId=" + gitlabProjectId +
                ", triggerUserId=" + triggerUserId +
                ", createdDate=" + createdDate +
                ", finishedDate=" + finishedDate +
                ", triggerType='" + triggerType + '\'' +
                ", bpmDefinition='" + bpmDefinition + '\'' +
                ", businessKey='" + businessKey + '\'' +
                ", edited=" + edited +
                ", auditUser='" + auditUser + '\'' +
                ", errorInfo='" + errorInfo + '\'' +
                '}';
    }
}
