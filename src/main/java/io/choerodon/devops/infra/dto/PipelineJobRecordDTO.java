package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 流水线任务记录(PipelineJobRecord)实体类
 *
 * @author
 * @since 2022-11-23 16:42:34
 */

@ApiModel("流水线任务记录")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_job_record")
public class PipelineJobRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_ID = "pipelineId";
    public static final String FIELD_JOB_ID = "jobId";
    public static final String FIELD_STAGE_RECORD_ID = "stageRecordId";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_LOG_ID = "logId";
    private static final long serialVersionUID = 940900444060009476L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "所属任务Id,devops_pipeline_job.id", required = true)
    @NotNull
    private Long jobId;

    @ApiModelProperty(value = "关联阶段记录Id,devops_pipeline_stage_record.id", required = true)
    @NotNull
    private Long stageRecordId;

    @ApiModelProperty(value = "状态", required = true)
    @NotBlank
    private String status;

    @ApiModelProperty(value = "关联日志记录Id,devops_pipeline_log.id")
    private Long logId;

    public PipelineJobRecordDTO() {
    }

    public PipelineJobRecordDTO(Long pipelineId, Long jobId, Long stageRecordId, String status) {
        this.pipelineId = pipelineId;
        this.jobId = jobId;
        this.stageRecordId = stageRecordId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

}

