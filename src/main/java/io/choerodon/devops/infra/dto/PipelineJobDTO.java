package io.choerodon.devops.infra.dto;

import javax.persistence.Column;
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
 * 流水线任务表(PipelineJob)实体类
 *
 * @author
 * @since 2022-11-24 15:55:44
 */

@ApiModel("流水线任务表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_job")
public class PipelineJobDTO extends AuditDomain {
    private static final long serialVersionUID = -20494543680459353L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_ID = "pipelineId";
    public static final String FIELD_VERSION_ID = "versionId";
    public static final String FIELD_STAGE_ID = "stageId";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_CONFIG_ID = "configId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "所属版本Id,devops_pipeline_version.id", required = true)
    @NotNull
    private Long versionId;

    @ApiModelProperty(value = "所属阶段Id,devops_pipeline_stage.id", required = true)
    @NotNull
    private Long stageId;

    @ApiModelProperty(value = "名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "任务类型", required = true)
    @NotBlank
    private String type;

    @ApiModelProperty(value = "关联任务配置Id")
    private Long configId;


    @Column(name = "is_enabled")
    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

}

