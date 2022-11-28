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
 * 流水线执行日志(PipelineLog)实体类
 *
 * @author
 * @since 2022-11-23 16:42:45
 */

@ApiModel("流水线执行日志")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_log")
public class PipelineLogDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_LOG = "log";
    private static final long serialVersionUID = 985180781271184522L;
    @Id
    @GeneratedValue
    private Long id;
    @ApiModelProperty(value = "devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "devops_pipeline.id", required = true)
    @NotNull
    private Long jobRecordId;

    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    @NotBlank
    private String log;

    public Long getJobRecordId() {
        return jobRecordId;
    }

    public void setJobRecordId(Long jobRecordId) {
        this.jobRecordId = jobRecordId;
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

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

}

