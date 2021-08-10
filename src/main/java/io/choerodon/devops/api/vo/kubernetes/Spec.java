package io.choerodon.devops.api.vo.kubernetes;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class Spec {
    @ApiModelProperty("这个实例所属的应用服务id/选填，由猪齿鱼前端页面创建的实例应该有这个值，便于网络选择此实例进行管理。被用于实例的label: choerodon.io/app-service-id")
    private Long appServiceId;
    /**
     * {@link io.choerodon.devops.infra.enums.AppServiceInstanceSource}
     */
    @ApiModelProperty("这个实例的来源/选填，默认为normal")
    private String source;

    @ApiModelProperty("应用市场的发布对象id/因为多个发布对象之间，chartVersion不一定发生了变化，所以需要这个来确定具体是部署哪个发布对象/应用市场实例需要，可为空")
    private Long marketDeployObjectId;

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

    @ApiModelProperty("1.1版本新增,解决agent部署自定义资源丢失精度问题")
    private String v1CommandId;
    @ApiModelProperty("1.1版本新增,解决agent部署自定义资源丢失精度问题")
    private String v1AppServiceId;


    public String getV1CommandId() {
        return v1CommandId;
    }

    public void setV1CommandId(String v1CommandId) {
        this.v1CommandId = v1CommandId;
    }

    public String getV1AppServiceId() {
        return v1AppServiceId;
    }

    public void setV1AppServiceId(String v1AppServiceId) {
        this.v1AppServiceId = v1AppServiceId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    public Long getMarketDeployObjectId() {
        return marketDeployObjectId;
    }

    public void setMarketDeployObjectId(Long marketDeployObjectId) {
        this.marketDeployObjectId = marketDeployObjectId;
    }
}
