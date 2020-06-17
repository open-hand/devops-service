package io.choerodon.devops.infra.dto;


import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_pv")
public class DevopsPvDTO extends AuditDomain {

    public static final String ENCRYPT_KEY = "devops_pv";
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @Encrypt(DevopsPvDTO.ENCRYPT_KEY)
    private Long id;

    @ApiModelProperty("projectId")
    private Long projectId;

    @ApiModelProperty("pv名称")
    private String name;

    @ApiModelProperty("pv类型")
    private String type;

    @ApiModelProperty("pv描述")
    private String description;

    @ApiModelProperty("pv状态")
    private String status;

    @ApiModelProperty("关联的pvcName")
    private String pvcName;

    @Transient
    private String clusterName;

    @Transient
    private Boolean clusterConnect;

    @ApiModelProperty("关联的clusterId")
    private Long clusterId;

    @ApiModelProperty("访问模式")
    private String accessModes;

    @ApiModelProperty("是否跳过权限校验")
    private Boolean skipCheckProjectPermission;

    @ApiModelProperty("资源大小")
    private String requestResource;

    @ApiModelProperty("操作命令id")
    private Long commandId;

    @ApiModelProperty(value = "所属集群的系统环境id", hidden = true)
    @Transient
    private Long envId;

    @ApiModelProperty("根据存储类型的不同，生成不同的Json数据")
    private String valueConfig;

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

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public String getRequestResource() {
        return requestResource;
    }

    public void setRequestResource(String requestResource) {
        this.requestResource = requestResource;
    }

    public String getAccessModes() {
        return accessModes;
    }

    public void setAccessModes(String accessModes) {
        this.accessModes = accessModes;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getPvcName() {
        return pvcName;
    }

    public void setPvcName(String pvcName) {
        this.pvcName = pvcName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getValueConfig() {
        return valueConfig;
    }

    public void setValueConfig(String valueConfig) {
        this.valueConfig = valueConfig;
    }

    public Boolean getClusterConnect() {
        return clusterConnect;
    }

    public void setClusterConnect(Boolean clusterConnect) {
        this.clusterConnect = clusterConnect;
    }

    @Override
    public String toString() {
        return "DevopsPvDTO{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", pvcName='" + pvcName + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", clusterConnect=" + clusterConnect +
                ", clusterId=" + clusterId +
                ", accessModes='" + accessModes + '\'' +
                ", skipCheckProjectPermission=" + skipCheckProjectPermission +
                ", requestResource='" + requestResource + '\'' +
                ", commandId=" + commandId +
                ", envId=" + envId +
                ", valueConfig='" + valueConfig + '\'' +
                '}';
    }
}
