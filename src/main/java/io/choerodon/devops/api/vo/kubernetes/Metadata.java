package io.choerodon.devops.api.vo.kubernetes;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class Metadata {
    @ApiModelProperty("创建时间")
    private Date creationTimestamp;
    @ApiModelProperty("名称")
    private String name;

    /**
     * 可选值参考 {@link io.choerodon.devops.infra.enums.C7NHelmReleaseMetadataType}
     */
    @ApiModelProperty("release类型/这个值选填，且只是由devops-service处理的元数据，正常部署应用服务时这个值为null即可，部署集群组件等没有应用服务版本的release是 cluster-component")
    private String type;

    @ApiModelProperty("这个实例所属的应用服务id/选填，由猪齿鱼前端页面创建的实例应该有这个值，便于网络选择此实例进行管理。被用于实例的label: choerodon.io/app-service-id")
    private Long appServiceId;

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }
}
