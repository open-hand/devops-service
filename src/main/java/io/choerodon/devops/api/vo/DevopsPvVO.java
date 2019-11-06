package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class DevopsPvVO {

    @ApiModelProperty("pvId")
    private Long id;

    @ApiModelProperty("PV名称")
    private String name;

    @ApiModelProperty("PV状态")
    private String status;

    @ApiModelProperty("详细描述")
    private String description;

    @ApiModelProperty("关联集群名称")
    private String clusterName;

    @ApiModelProperty("PV存储类型")
    private String type;

    @ApiModelProperty("关联PVC名称")
    private Long pvcName;

    @ApiModelProperty("访问模式")
    private String accessModes;

    @ApiModelProperty("资源请求大小")
    private String requestResource;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getPvcName() {
        return pvcName;
    }

    public void setPvcName(Long pvcName) {
        this.pvcName = pvcName;
    }

    public String getAccessModes() {
        return accessModes;
    }

    public void setAccessModes(String accessModes) {
        this.accessModes = accessModes;
    }

    public String getRequestResource() {
        return requestResource;
    }

    public void setRequestResource(String requestResource) {
        this.requestResource = requestResource;
    }
}
