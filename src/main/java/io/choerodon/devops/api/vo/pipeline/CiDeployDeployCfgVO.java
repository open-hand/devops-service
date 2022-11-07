package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author hao.wang@zknow.com
 * @since 2022-11-07 10:18:26
 */
public class CiDeployDeployCfgVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "环境Id,devops_env.id", required = true)
    private Long envId;
    @ApiModelProperty(value = "部署类型：create 新建实例， update 替换实例", required = true)
    private String deployType;
    @ApiModelProperty(value = "是否校验环境权限", required = true)
    private Object skipCheckPermission;
    @ApiModelProperty(value = "应用名称,devops_deploy_app_center_env.name", required = true)
    private String appName;
    @ApiModelProperty(value = "应用编码,devops_deploy_app_center_env.code", required = true)
    private String appCode;
    @ApiModelProperty(value = "应用配置", required = true)
    private String appConfigJson;
    @ApiModelProperty(value = "容器配置", required = true)
    private String containerConfigJson;


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

    public Object getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Object skipCheckPermission) {
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

    public String getAppConfigJson() {
        return appConfigJson;
    }

    public void setAppConfigJson(String appConfigJson) {
        this.appConfigJson = appConfigJson;
    }

    public String getContainerConfigJson() {
        return containerConfigJson;
    }

    public void setContainerConfigJson(String containerConfigJson) {
        this.containerConfigJson = containerConfigJson;
    }
}
