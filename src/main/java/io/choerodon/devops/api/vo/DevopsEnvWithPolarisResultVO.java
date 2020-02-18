package io.choerodon.devops.api.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

/**
 * 带有扫描结果的环境数据
 *
 * @author zmf
 * @since 2/18/20
 */
public class DevopsEnvWithPolarisResultVO {
    @ApiModelProperty("namespace")
    private String namespace;
    @ApiModelProperty("是否是内部环境(猪齿鱼管理的环境)")
    private Boolean internal;
    @ApiModelProperty("namespace下是否有error级别的配置项")
    private Boolean hasErrors;
    @ApiModelProperty("环境id / 可为空")
    private Long envId;
    @ApiModelProperty("环境名称 / 可为空")
    private String envName;
    @ApiModelProperty("项目id / 可为空")
    private Long projectId;
    @ApiModelProperty("项目名称 / 可为空")
    private String projectName;
    @ApiModelProperty("项目code / 可为空")
    private String projectCode;
    @ApiModelProperty("扫描结果json")
    private List<String> detailJson;
    @JsonIgnore
    @ApiModelProperty("每个item是否有error")
    private List<Boolean> itemHasErrors;
    @ApiModelProperty("是否扫描过")
    private Boolean checked;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public List<String> getDetailJson() {
        return detailJson;
    }

    public void setDetailJson(List<String> detailJson) {
        this.detailJson = detailJson;
    }

    public List<Boolean> getItemHasErrors() {
        return itemHasErrors;
    }

    public void setItemHasErrors(List<Boolean> itemHasErrors) {
        this.itemHasErrors = itemHasErrors;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
