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
 * 流水线执行记录(PipelineRecord)实体类
 *
 * @author
 * @since 2022-11-23 16:43:01
 */

@ApiModel("流水线执行记录")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_record")
public class PipelineRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_PIPELINE_ID = "pipelineId";
    public static final String FIELD_STATUS = "status";
    private static final long serialVersionUID = 286315842540628499L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    @NotNull
    private Long pipelineId;

    @ApiModelProperty(value = "状态", required = true)
    @NotBlank
    private String status;
    @ApiModelProperty(value = "触发方式", required = true)
    private String triggerType;
    @ApiModelProperty(value = "触发应用服务id,devops_app_service.id", required = true)
    private Long appServiceId;
    @ApiModelProperty(value = "触发应用服务版本id,devops_app_service_version.id", required = true)
    private Long appServiceVersion;

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getAppServiceVersion() {
        return appServiceVersion;
    }

    public void setAppServiceVersion(Long appServiceVersion) {
        this.appServiceVersion = appServiceVersion;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

