package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author lihao
 * @date 2019-09-15 12:52
 */
public class AppServiceDeployUpdateVO {
    @Encrypt
    @ApiModelProperty("服务id/必填")
    @NotNull(message = "{devops.app.id.null}")
    private Long appServiceId;

    @Encrypt
    @ApiModelProperty("服务应用版本id/必填")
    @NotNull(message = "{devops.app.version.id.null}")
    private Long appServiceVersionId;

    @Encrypt
    @ApiModelProperty("环境id/必填")
    @NotNull(message = "{devops.env.id.null}")
    private Long environmentId;

    @Encrypt
    @ApiModelProperty("实例id/必填")
    @NotNull(message = "{devops.app.instance.id.null}")
    private Long instanceId;

    @ApiModelProperty("部署配置/必填")
    @NotBlank(message = "{devops.app.instance.values.null}")
    private String values;

    @ApiModelProperty("操作类型")
    private String type;

    @ApiModelProperty("应用中心应用名称")
    private String appName;

    @ApiModelProperty("replicas生效策略在 values/deployment")
    private  String replicasStrategy;

    public String getReplicasStrategy() {
        return replicasStrategy;
    }

    public void setReplicasStrategy(String replicasStrategy) {
        this.replicasStrategy = replicasStrategy;
    }

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

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
