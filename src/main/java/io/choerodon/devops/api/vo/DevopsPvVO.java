package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.DevopsPvDTO;

public class DevopsPvVO {

    @ApiModelProperty("pvId")
//    @Encrypt(DevopsPvDTO.ENCRYPT_KEY)
    private Long id;

    @ApiModelProperty("关联的clusterId")
    private Long clusterId;

    @ApiModelProperty("所属项目id")
    private Long projectId;

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
    private String pvcName;

    @ApiModelProperty("访问模式")
    private String accessModes;

    @ApiModelProperty("资源请求大小")
    private String requestResource;

    @ApiModelProperty("是否跳过权限校验")
    private Boolean skipCheckProjectPermission;

    @ApiModelProperty("版本号")
    private Long ObjectVersionNumber;

    @ApiModelProperty("集群连接状态")
    private Boolean clusterConnect;

    public Boolean getClusterConnect() {
        return clusterConnect;
    }

    public void setClusterConnect(Boolean clusterConnect) {
        this.clusterConnect = clusterConnect;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

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

    public String getPvcName() {
        return pvcName;
    }

    public void setPvcName(String pvcName) {
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

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getObjectVersionNumber() {
        return ObjectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        ObjectVersionNumber = objectVersionNumber;
    }

    @Override
    public String toString() {
        return "DevopsPvVO{" +
                "id=" + id +
                ", clusterId=" + clusterId +
                ", projectId=" + projectId +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", type='" + type + '\'' +
                ", pvcName='" + pvcName + '\'' +
                ", accessModes='" + accessModes + '\'' +
                ", requestResource='" + requestResource + '\'' +
                ", skipCheckProjectPermission=" + skipCheckProjectPermission +
                ", ObjectVersionNumber=" + ObjectVersionNumber +
                '}';
    }
}
