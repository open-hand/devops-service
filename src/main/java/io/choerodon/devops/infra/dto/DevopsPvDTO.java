package io.choerodon.devops.infra.dto;


import io.choerodon.mybatis.entity.BaseDTO;

import javax.persistence.Table;

@Table(name = "devops_pv")
public class DevopsPvDTO extends BaseDTO {

    private Long id;
    private Long name;
    private String type;
    private String description;
    private String status;
    private Long pvcId;
    private Long clusterId;
    private Boolean skipCheckProjectPermission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getName() {
        return name;
    }

    public void setName(Long name) {
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

    public Long getPvcId() {
        return pvcId;
    }

    public void setPvcId(Long pvcId) {
        this.pvcId = pvcId;
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
}
