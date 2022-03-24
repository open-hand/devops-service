package io.choerodon.devops.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * devops_ci_schedule_variable(CiScheduleVariable)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:52
 */

@ApiModel("devops_ci_schedule_variable")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_schedule_variable")
public class CiScheduleVariableDTO extends AuditDomain {
    private static final long serialVersionUID = 774599804372059533L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_CI_PIPELINE_SCHEDULE_ID = "ciPipelineScheduleId";
    public static final String FIELD_PIPELINE_SCHEDULE_ID = "pipelineScheduleId";
    public static final String FIELD_KEY = "key";
    public static final String FIELD_VALUE = "value";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops 定时计划id", required = true)
    @NotNull
    private Long ciPipelineScheduleId;

    @ApiModelProperty(value = "gitlab pipeline_schedule_id", required = true)
    @NotNull
    private Long pipelineScheduleId;

    @ApiModelProperty(value = "key", required = true)
    @NotBlank
    private String key;

    @ApiModelProperty(value = "value", required = true)
    @NotBlank
    private String value;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiPipelineScheduleId() {
        return ciPipelineScheduleId;
    }

    public void setCiPipelineScheduleId(Long ciPipelineScheduleId) {
        this.ciPipelineScheduleId = ciPipelineScheduleId;
    }

    public Long getPipelineScheduleId() {
        return pipelineScheduleId;
    }

    public void setPipelineScheduleId(Long pipelineScheduleId) {
        this.pipelineScheduleId = pipelineScheduleId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

