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
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 流水线表(Pipeline)实体类
 *
 * @author
 * @since 2022-11-24 15:50:12
 */

@ApiModel("流水线表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline")
public class PipelineDTO extends AuditDomain {
    private static final long serialVersionUID = 283632600120793678L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_PROJECT_ID = "projectId";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_EFFECT_VERSION_ID = "effectVersionId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "项目id", required = true)
    @NotNull
    private Long projectId;

    @ApiModelProperty(value = "流水线名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "当前生效的版本，devops_pipeline_version.id", required = true)
    @Encrypt
    private Long effectVersionId;

    @ApiModelProperty(value = "令牌", required = false)
    private String token;

    @ApiModelProperty(value = "是否启用", required = false)
    @Column(name = "is_enable")
    private Boolean enable;
    @ApiModelProperty(value = "是否开启应用服务版本生成触发", required = false)
    @Column(name = "is_app_version_trigger_enable")
    private Boolean appVersionTriggerEnable;

    public Boolean getAppVersionTriggerEnable() {
        return appVersionTriggerEnable;
    }

    public void setAppVersionTriggerEnable(Boolean appVersionTriggerEnable) {
        this.appVersionTriggerEnable = appVersionTriggerEnable;
    }


    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getEffectVersionId() {
        return effectVersionId;
    }

    public void setEffectVersionId(Long effectVersionId) {
        this.effectVersionId = effectVersionId;
    }

}

