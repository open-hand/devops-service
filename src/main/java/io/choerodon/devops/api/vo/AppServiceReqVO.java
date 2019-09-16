package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by younger on 2018/3/28.
 */
public class AppServiceReqVO {

    private Long id;
    @ApiModelProperty("服务名称/必填")
    @NotNull(message = "error.app.name.null")
    private String name;
    @ApiModelProperty("服务code/必填")
    @NotNull(message = "error.app.code.null")
    private String code;
    @ApiModelProperty("项目id/必填")
    @NotNull(message = "error.project.id.null")
    private Long projectId;
    @ApiModelProperty("服务类型/必填")
    @NotNull(message = "error.app.type.null")
    private String type;
    @ApiModelProperty("模板服务Id")
    private Long templateAppServiceId;
    @ApiModelProperty("模板服务版本Id")
    private Long templateAppServiceVersionId;
    private Long harborConfigId;
    private Long chartConfigId;
    private String imgUrl;

    public Long getTemplateAppServiceId() {
        return templateAppServiceId;
    }

    public void setTemplateAppServiceId(Long templateAppServiceId) {
        this.templateAppServiceId = templateAppServiceId;
    }

    public Long getTemplateAppServiceVersionId() {
        return templateAppServiceVersionId;
    }

    public void setTemplateAppServiceVersionId(Long templateAppServiceVersionId) {
        this.templateAppServiceVersionId = templateAppServiceVersionId;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getHarborConfigId() {
        return harborConfigId;
    }

    public void setHarborConfigId(Long harborConfigId) {
        this.harborConfigId = harborConfigId;
    }

    public Long getChartConfigId() {
        return chartConfigId;
    }

    public void setChartConfigId(Long chartConfigId) {
        this.chartConfigId = chartConfigId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
