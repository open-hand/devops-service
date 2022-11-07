package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author hao.wang@zknow.com
 * @since 2022-11-04 14:50:19
 */
public class CiChartDeployConfigVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "应用idId", required = true)
    @Encrypt
    private Long appId;
    @ApiModelProperty(value = "环境Id,devops_env.id", required = true)
    @Encrypt
    private Long envId;
    @ApiModelProperty(value = "valueId,devops_deploy_value.id", required = true)
    @Encrypt
    private Long valueId;
    @ApiModelProperty(value = "部署类型：create 新建实例， update 替换实例", required = true)
    private String deployType;
    @ApiModelProperty(value = "是否校验环境权限", required = true)
    private Boolean skipCheckPermission;
    @ApiModelProperty(value = "应用名称,devops_deploy_app_center_env.name", required = true)
    private String appName;
    @ApiModelProperty(value = "应用编码,devops_deploy_app_center_env.code", required = true)
    private String appCode;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}
