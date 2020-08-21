package io.choerodon.devops.api.vo.kubernetes;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class Spec {
    @ApiModelProperty("这个实例所属的应用服务id/选填，由猪齿鱼前端页面创建的实例应该有这个值，便于网络选择此实例进行管理。被用于实例的label: choerodon.io/app-service-id")
    private Long appServiceId;

    @ApiModelProperty("char仓库地址")
    private String repoUrl;
    @ApiModelProperty("chart包名")
    private String chartName;
    @ApiModelProperty("命令id")
    private Long commandId;
    @ApiModelProperty("镜像拉取校验secret")
    private List<ImagePullSecret> imagePullSecrets;
    @ApiModelProperty("chart版本")
    private String chartVersion;
    @ApiModelProperty("部署配置内容")
    private String values;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public List<ImagePullSecret> getImagePullSecrets() {
        return imagePullSecrets;
    }

    public void setImagePullSecrets(List<ImagePullSecret> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
}
