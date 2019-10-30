package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.entity.BaseDTO;

import javax.persistence.Table;

@Table(name = "devops_cluster_resource")
public class DevopsClusterResourceDTO extends BaseDTO {
    private Long id;
    private String name;
    private String code;
    private String type;
    private Long clusterId;
    private Long objectId;
    private Long configId;
    private Long systemEnvId;
    public Long getSystemEnvId() {
        return systemEnvId;
    }

    public void setSystemEnvId(Long systemEnvId) {
        this.systemEnvId = systemEnvId;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

}
