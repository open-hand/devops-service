package io.choerodon.devops.api.vo.cd;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.AppServiceRepVO;

/**
 * @author
 * @since 2022-11-24 16:55:35
 */
public class PipelineChartDeployCfgVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "流水线id", required = true)
    private Long pipelineId;
    @ApiModelProperty(value = "环境Id,devops_env.id", required = true)
    @Encrypt
    private Long envId;
    @ApiModelProperty(value = "应用服务Id,devops_app_service.id", required = true)
    @Encrypt
    private Long appServiceId;
    @ApiModelProperty(value = "部署版本类型")
    private String version;
    @ApiModelProperty(value = "valueId,devops_deploy_value.id", required = true)
    @Encrypt
    private Long valueId;
    @ApiModelProperty(value = "部署类型：create 新建实例， update 替换实例", required = true)
    private String deployType;
    @ApiModelProperty(value = "是否校验环境权限", required = true)
    private Boolean skipCheckPermission;
    @ApiModelProperty(value = "应用id,devops_deploy_app_center_env.id")
    @Encrypt
    private Long appId;
    @ApiModelProperty(value = "应用名称,devops_deploy_app_center_env.name")
    private String appName;
    @ApiModelProperty(value = "应用编码,devops_deploy_app_center_env.code")
    private String appCode;

    @ApiModelProperty(value = "应用名称")
    private String appServiceName;

    @ApiModelProperty(value = "环境名称")
    private String envName;

    @ApiModelProperty(value = "部署配置")
    private String value;
    private AppServiceRepVO appServiceDTO;


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AppServiceRepVO getAppServiceDTO() {
        return appServiceDTO;
    }

    public void setAppServiceDTO(AppServiceRepVO appServiceDTO) {
        this.appServiceDTO = appServiceDTO;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
