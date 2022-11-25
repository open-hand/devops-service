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
 * 流水线阶段记录(PipelineStageRecord)实体类
 *
 * @author
 * @since 2022-11-23 16:43:12
 */

@ApiModel("流水线阶段记录")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_stage_record")
public class PipelineStageRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_ID = "pipelineId";
    public static final String FIELD_STAGE_ID = "stageId";
    public static final String FIELD_PIPELINE_RECORD_ID = "pipelineRecordId";
    public static final String FIELD_STATUS = "status";
    private static final long serialVersionUID = 419144782197891150L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "所属阶段Id,devops_pipeline_stage.id", required = true)
    @NotNull
    private Long stageId;

    @ApiModelProperty(value = "阶段顺序", required = true)
    private Integer sequence;

    @ApiModelProperty(value = "关联流水线记录Id,devops_pipeline_record.id", required = true)
    @NotNull
    private Long pipelineRecordId;

    @ApiModelProperty(value = "状态", required = true)
    @NotBlank
    private String status;

    public PipelineStageRecordDTO() {
    }

    public PipelineStageRecordDTO(Long pipelineId, Long stageId, Long pipelineRecordId, Integer sequence, String status) {
        this.pipelineId = pipelineId;
        this.stageId = stageId;
        this.sequence = sequence;
        this.pipelineRecordId = pipelineRecordId;
        this.status = status;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
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

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

