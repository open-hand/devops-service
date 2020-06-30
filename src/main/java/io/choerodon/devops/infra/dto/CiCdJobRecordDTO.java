package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.*;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "cicd_job_record")
public class CiCdJobRecordDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty(name = "gitlab_job_id")
    private Long gitlabJobId;
    @ApiModelProperty(name = "gitlab_流水线记录id")
    private Long gitlabPipelineId;
    @ApiModelProperty(name = "流水线记录Id")
    private Long cicdPipelineRecordId;
    @ApiModelProperty(name = "job详细信息，定义了job执行内容")
    private String metadata;
    @ApiModelProperty(name = "任务名称")
    private String name;
    @ApiModelProperty(name = "所属阶段名称")
    private String stage;
    @ApiModelProperty(name = "job状态")
    private String status;
    @ApiModelProperty(name = "触发用户id")
    private Long triggerUserId;
    @ApiModelProperty(name = "'触发版本")
    private String triggerVersion;
    @ApiModelProperty(name = "job开始执行时间")
    private Date startedDate;
    @ApiModelProperty(name = "job结束时间")
    private Date finishedDate;
    @ApiModelProperty(name = "任务类型")
    private String type;
    @ApiModelProperty(name = "job执行时长")
    private Long durationSeconds;
    @ApiModelProperty(name = "gitlab_project_id")
    private Long gitlabProjectId;
    @ApiModelProperty(name = "app_service_id")
    private Long appServiceId;
    @ApiModelProperty(name = "cicd_job_id")
    private Long cicdJobId;
    @ApiModelProperty(name = "cicd_stage_record_id")
    private Long cicdStageRecordId;
    @ApiModelProperty(name = "环境Id")
    private Long envId;
    @ApiModelProperty(name = "实例Id")
    private Long instanceId;
    @ApiModelProperty(name = "版本Id")
    private Long versionId;
    @ApiModelProperty(name = "project_id")
    private Long projectId;
    @ApiModelProperty(name = "app_service_deploy_id")
    private Long appServiceDeployId;
    @ApiModelProperty(name = "value_id")
    private Long valueId;
    @ApiModelProperty(name = "审核人员")
    private String auditUser;
    @ApiModelProperty(name = "是否会签")
    @Column(name = "is_countersigned")
    private Boolean countersigned;
    @ApiModelProperty(name = "配置信息")
    private String value;
    @ApiModelProperty(name = "实例名称")
    private String instanceName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabJobId() {
        return gitlabJobId;
    }

    public void setGitlabJobId(Long gitlabJobId) {
        this.gitlabJobId = gitlabJobId;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public Long getCicdPipelineRecordId() {
        return cicdPipelineRecordId;
    }

    public void setCicdPipelineRecordId(Long cicdPipelineRecordId) {
        this.cicdPipelineRecordId = cicdPipelineRecordId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTriggerUserId() {
        return triggerUserId;
    }

    public void setTriggerUserId(Long triggerUserId) {
        this.triggerUserId = triggerUserId;
    }

    public String getTriggerVersion() {
        return triggerVersion;
    }

    public void setTriggerVersion(String triggerVersion) {
        this.triggerVersion = triggerVersion;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getCicdJobId() {
        return cicdJobId;
    }

    public void setCicdJobId(Long cicdJobId) {
        this.cicdJobId = cicdJobId;
    }

    public Long getCicdStageRecordId() {
        return cicdStageRecordId;
    }

    public void setCicdStageRecordId(Long cicdStageRecordId) {
        this.cicdStageRecordId = cicdStageRecordId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAppServiceDeployId() {
        return appServiceDeployId;
    }

    public void setAppServiceDeployId(Long appServiceDeployId) {
        this.appServiceDeployId = appServiceDeployId;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(String auditUser) {
        this.auditUser = auditUser;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Override
    public String toString() {
        return "CiCdJobRecordDTO{" +
                "id=" + id +
                ", gitlabJobId=" + gitlabJobId +
                ", gitlabPipelineId=" + gitlabPipelineId +
                ", cicdPipelineRecordId=" + cicdPipelineRecordId +
                ", metadata='" + metadata + '\'' +
                ", name='" + name + '\'' +
                ", stage='" + stage + '\'' +
                ", status='" + status + '\'' +
                ", triggerUserId=" + triggerUserId +
                ", triggerVersion='" + triggerVersion + '\'' +
                ", startedDate=" + startedDate +
                ", finishedDate=" + finishedDate +
                ", type='" + type + '\'' +
                ", durationSeconds=" + durationSeconds +
                ", gitlabProjectId=" + gitlabProjectId +
                ", appServiceId=" + appServiceId +
                ", cicdJobId=" + cicdJobId +
                ", cicdStageRecordId=" + cicdStageRecordId +
                ", envId=" + envId +
                ", instanceId=" + instanceId +
                ", versionId=" + versionId +
                ", projectId=" + projectId +
                ", appServiceDeployId=" + appServiceDeployId +
                ", valueId=" + valueId +
                ", auditUser='" + auditUser + '\'' +
                ", countersigned=" + countersigned +
                ", value='" + value + '\'' +
                ", instanceName='" + instanceName + '\'' +
                '}';
    }
}
