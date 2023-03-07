package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 人工卡点审核记录表(PipelineAuditRecord)实体类
 *
 * @author
 * @since 2022-11-23 16:42:03
 */

@ApiModel("人工卡点审核记录表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_audit_record")
public class PipelineAuditRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_JOB_RECORD_ID = "jobRecordId";
    public static final String FIELD_COUNTERSIGNED = "countersigned";
    private static final long serialVersionUID = -98607536214731369L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "devops_pipeline_record.id", required = true)
    @NotNull
    private Long pipelineRecordId;

    @ApiModelProperty(value = "devops_pipeline_job_record.id", required = true)
    @NotNull
    private Long jobRecordId;

    @ApiModelProperty(value = "是否会签 1是会签,0 是或签", required = true)
    @NotNull
    private Boolean countersigned;


    public PipelineAuditRecordDTO() {
    }

    public PipelineAuditRecordDTO(Long pipelineId, Long pipelineRecordId, Long jobRecordId, Boolean countersigned) {
        this.pipelineId = pipelineId;
        this.pipelineRecordId = pipelineRecordId;
        this.jobRecordId = jobRecordId;
        this.countersigned = countersigned;
    }

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobRecordId() {
        return jobRecordId;
    }

    public void setJobRecordId(Long jobRecordId) {
        this.jobRecordId = jobRecordId;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }
}

