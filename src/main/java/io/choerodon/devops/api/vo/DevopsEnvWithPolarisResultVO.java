package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 带有扫描结果的环境数据
 *
 * @author zmf
 * @since 2/18/20
 */
public class DevopsEnvWithPolarisResultVO {
    @ApiModelProperty("namespace")
    private String namespace;
    @ApiModelProperty(value = "是否是内部环境(猪齿鱼管理的环境)", hidden = true)
    @JsonIgnore
    private Boolean internal;
    @ApiModelProperty("namespace下是否有error级别的配置项")
    private Boolean hasErrors;
    @Encrypt
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
    /**
     * 是{@link io.choerodon.devops.api.vo.polaris.PolarisStorageControllerResultVO} 的数组
     */
    @ApiModelProperty("namespace下的配置文件数据的结构")
    private String detailJson;

    @ApiModelProperty("是否扫描过")
    private Boolean checked;

    public DevopsEnvWithPolarisResultVO() {
    }

    public DevopsEnvWithPolarisResultVO(String namespace, Boolean internal, Boolean checked, String detailJson) {
        this.namespace = namespace;
        this.internal = internal;
        this.checked = checked;
        this.detailJson = detailJson;
    }

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

    public String getDetailJson() {
        return detailJson;
    }

    public void setDetailJson(String detailJson) {
        this.detailJson = detailJson;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
