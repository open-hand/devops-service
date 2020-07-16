package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 批量部署纪录的实例详情
 */
public class AppServiceInstanceForRecordVO {
    @Encrypt
    @ApiModelProperty("实例id")
    private Long instanceId;

    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long appServiceId;

    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;

    @ApiModelProperty("应用服务名称")
    private String appServiceName;
    @ApiModelProperty("实例部署时版本")
    private String appServiceVersion;
    @ApiModelProperty("环境名称")
    private String envName;
    @ApiModelProperty("实例名称")
    private String instanceName;
    @ApiModelProperty("是否删除")
    private Boolean deleted;

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceVersion() {
        return appServiceVersion;
    }

    public void setAppServiceVersion(String appServiceVersion) {
        this.appServiceVersion = appServiceVersion;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
