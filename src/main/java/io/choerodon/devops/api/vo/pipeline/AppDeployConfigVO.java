package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/7 16:16
 */
public class AppDeployConfigVO {
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "环境Id,devops_env.id", required = true)
    @Encrypt
    private Long envId;
    @ApiModelProperty(value = "envName")
    private String envName;
    @ApiModelProperty(value = "部署类型：create 新建实例， update 替换实例", required = true)
    private String deployType;
    @ApiModelProperty(value = "是否校验环境权限", required = true)
    private Boolean skipCheckPermission;
    @ApiModelProperty(value = "应用idId", required = true)
    @Encrypt
    private Long appId;
    @ApiModelProperty(value = "应用名称,devops_deploy_app_center_env.name", required = true)
    private String appName;
    @ApiModelProperty(value = "应用编码,devops_deploy_app_center_env.code", required = true)
    private String appCode;

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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
