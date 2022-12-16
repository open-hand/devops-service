package io.choerodon.devops.infra.dto;

import java.util.Date;
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
    @ApiModelProperty(value = "名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "所属项目Id", required = true)
    @NotNull
    private Long projectId;

    @ApiModelProperty(value = "所属任务Id,devops_pipeline_job.id", required = true)
    @NotNull
    private Long jobId;

    @ApiModelProperty(value = "关联流水线记录Id,devops_pipeline_record.id", required = true)
    @NotNull
    private Long pipelineRecordId;

    @ApiModelProperty(value = "关联阶段记录Id,devops_pipeline_stage_record.id", required = true)
    @NotNull
    private Long stageRecordId;

    @ApiModelProperty(value = "状态", required = true)
    @NotBlank
    private String status;
    @ApiModelProperty(value = "任务开始时间", required = true)
    private Date startedDate;
    @ApiModelProperty(value = "任务结束时间", required = true)
    private Date finishedDate;

    @ApiModelProperty(value = "任务类型", required = true)
    @NotBlank
    private String type;

    private Long commandId;

    public PipelineJobRecordDTO() {
    }

    public PipelineJobRecordDTO(Long projectId, Long pipelineId, Long jobId, String name, Long pipelineRecordId, Long stageRecordId, String status, String type) {
        this.projectId = projectId;
        this.pipelineId = pipelineId;
        this.name = name;
        this.jobId = jobId;
        this.pipelineRecordId = pipelineRecordId;
        this.stageRecordId = stageRecordId;
        this.status = status;
        this.type = type;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
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

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
    }
}

