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
 * 流水线模板表(PipelineTemplate)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 16:54:19
 */

@ApiModel("流水线模板表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_pipeline_template")
public class PipelineTemplateDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SOURCE_TYPE = "sourceType";
    public static final String FIELD_SOURCE_ID = "sourceId";
    public static final String FIELD_BUILT_IN = "builtIn";
    public static final String FIELD_CI_TEMPLATE_LANGUAGE_ID = "ciTemplateLanguageId";
    public static final String FIELD_ENABLE = "enable";
    public static final String FIELD_VERSION_NAME = "versionName";
    public static final String FIELD_RUNNER_IMAGES = "runnerImages";
    private static final long serialVersionUID = 358124236671616581L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "流水线模板名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String sourceType;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Long sourceId;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    @NotNull
    private Boolean builtIn;

    @ApiModelProperty(value = "关联语言id", required = true)
    @NotNull
    private Long ciTemplateCategoryId;

    @ApiModelProperty(value = "是否启用", required = true)
    @NotNull
    private Long enable;

    @ApiModelProperty(value = "版本命名规则", required = true)
    @NotBlank
    private String versionName;

    @ApiModelProperty(value = "流水线模板镜像地址", required = true)
    @NotBlank
    private String image;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }

    public Long getCiTemplateCategoryId() {
        return ciTemplateCategoryId;
    }

    public void setCiTemplateCategoryId(Long ciTemplateCategoryId) {
        this.ciTemplateCategoryId = ciTemplateCategoryId;
    }

    public Long getEnable() {
        return enable;
    }

    public void setEnable(Long enable) {
        this.enable = enable;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

