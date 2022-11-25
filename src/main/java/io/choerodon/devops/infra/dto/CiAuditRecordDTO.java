package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * ci 人工卡点审核记录表(CiAuditRecord)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-03 10:16:50
 */

@ApiModel("ci 人工卡点审核记录表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_audit_record")
public class CiAuditRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_SERVICE_ID = "appServiceId";
    public static final String FIELD_JOB_RECORD_ID = "jobRecordId";
    public static final String FIELD_GITLAB_PIPELINE_ID = "gitlabPipelineId";
    public static final String FIELD_JOB_NAME = "jobName";
    public static final String FIELD_COUNTERSIGNED = "countersigned";
    private static final long serialVersionUID = 237480699061673958L;
    @Id
    @Encrypt
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "关联devops流水线id")
    private Long ciPipelineId;

    @ApiModelProperty(value = "devops_app_service.id", required = true)
    @NotNull
    @Encrypt
    private Long appServiceId;

    @ApiModelProperty(value = "devops_ci_job_record.id", required = true)
    @NotNull
    @Encrypt
    private Long jobRecordId;

    @ApiModelProperty(value = "gitlabPipelineId", required = true)
    @NotNull
    private Long gitlabPipelineId;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    private String jobName;

    @ApiModelProperty(value = "是否会签 1是会签,0 是或签", required = true)
    private Boolean countersigned;

    public CiAuditRecordDTO() {
    }

    public CiAuditRecordDTO(@NotNull Long appServiceId, @NotNull Long gitlabPipelineId, @NotBlank String jobName) {
        this.appServiceId = appServiceId;
        this.gitlabPipelineId = gitlabPipelineId;
        this.jobName = jobName;
    }

    public CiAuditRecordDTO(Long ciPipelineId, @NotNull Long appServiceId, @NotNull Long jobRecordId, @NotNull Long gitlabPipelineId, @NotBlank String jobName, Boolean countersigned) {
        this.ciPipelineId = ciPipelineId;
        this.appServiceId = appServiceId;
        this.jobRecordId = jobRecordId;
        this.gitlabPipelineId = gitlabPipelineId;
        this.jobName = jobName;
        this.countersigned = countersigned;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getJobRecordId() {
        return jobRecordId;
    }

    public void setJobRecordId(Long jobRecordId) {
        this.jobRecordId = jobRecordId;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }
}

