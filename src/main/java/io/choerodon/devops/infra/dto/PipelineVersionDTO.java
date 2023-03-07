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
 * 流水线版本表(PipelineVersion)实体类
 *
 * @author
 * @since 2022-11-24 15:57:18
 */

@ApiModel("流水线版本表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_version")
public class PipelineVersionDTO extends AuditDomain {
    private static final long serialVersionUID = 398546159856120588L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_ID = "pipelineId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "流水线Id,devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;


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

}

