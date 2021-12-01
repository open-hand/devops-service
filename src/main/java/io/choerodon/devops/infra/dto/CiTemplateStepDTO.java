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
 * 流水线步骤模板(CiTemplateStep)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */

@ApiModel("流水线步骤模板")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_step")
public class CiTemplateStepDTO extends AuditDomain {
    private static final long serialVersionUID = 934315200817264859L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SOURCE_TYPE = "sourceType";
    public static final String FIELD_SOURCE_ID = "sourceId";
    public static final String FIELD_BUILT_IN = "builtIn";
    public static final String FIELD_CATEGORY_ID = "categoryId";
    public static final String FIELD_STEP_TYPE = "stepType";
    public static final String FIELD_SCRIPT = "script";
    public static final String FIELD_STEP_CONFIG_ID = "stepConfigId";

    @Id
    @GeneratedValue
    private Object id;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String sourceType;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Object sourceId;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    @NotNull
    private Object builtIn;

    @ApiModelProperty(value = "流水线步骤分类id", required = true)
    @NotNull
    private Object categoryId;

    @ApiModelProperty(value = "步骤类型", required = true)
    @NotBlank
    private String stepType;

    @ApiModelProperty(value = "自定义步骤的脚本", required = true)
    @NotBlank
    private String script;

    @ApiModelProperty(value = "具体步骤参数配置Id", required = true)
    @NotNull
    private Object stepConfigId;


    public Object getId() {
        return id;
    }

    public void setId(Object id) {
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

    public Object getSourceId() {
        return sourceId;
    }

    public void setSourceId(Object sourceId) {
        this.sourceId = sourceId;
    }

    public Object getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Object builtIn) {
        this.builtIn = builtIn;
    }

    public Object getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Object categoryId) {
        this.categoryId = categoryId;
    }

    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Object getStepConfigId() {
        return stepConfigId;
    }

    public void setStepConfigId(Object stepConfigId) {
        this.stepConfigId = stepConfigId;
    }

}

