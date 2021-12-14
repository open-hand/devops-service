package io.choerodon.devops.api.vo.template;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang08@hand-china.com
 * @since 2021-12-03 10:56:27
 */
public class CiTemplateStepVO {


    private Long id;
    @ApiModelProperty(value = "任务名称", required = true)
    private String name;
    @ApiModelProperty(value = "层级", required = true)
    private String sourceType;
    @ApiModelProperty(value = "层级Id", required = true)
    private Long sourceId;
    @ApiModelProperty(value = "流水线步骤分类id", required = true)
    private Long categoryId;
    @ApiModelProperty(value = "步骤类型", required = true)
    private String type;
    @ApiModelProperty(value = "自定义步骤的脚本", required = true)
    private String script;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    private Boolean builtIn;


    @ApiModelProperty(value = "所属任务id", required = true)
    @NotNull
    private Long ciTemplateJobId;
    @ApiModelProperty(value = "任务中的顺序", required = true)
    @NotNull
    private Long sequence;

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getCiTemplateJobId() {
        return ciTemplateJobId;
    }

    public void setCiTemplateJobId(Long ciTemplateJobId) {
        this.ciTemplateJobId = ciTemplateJobId;
    }

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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }
}