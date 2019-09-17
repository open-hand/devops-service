package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author lihao
 * @date 2019-09-15 12:52
 */
public class AppServiceDeployUpdateVO {
    @ApiModelProperty("服务id/必填")
    @NotNull(message = "error.app.id.null")
    private Long appServiceId;

    @ApiModelProperty("服务应用版本id/必填")
    @NotNull(message = "error.app.version.id.null")
    private Long appServiceVersionId;

    @ApiModelProperty("环境id/必填")
    @NotNull(message = "error.env.id.null")
    private Long environmentId;

    @ApiModelProperty("实例id/必填")
    @NotNull(message = "error.app.instance.id.null")
    private Long instanceId;

    @ApiModelProperty("部署配置/必填")
    @NotBlank(message = "error.app.instance.values.null")
    private String values;

    @ApiModelProperty("操作类型")
    private String type;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
