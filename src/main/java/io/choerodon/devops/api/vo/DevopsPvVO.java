package io.choerodon.devops.api.vo;

public class DevopsPvVO {

    private Long id;
    private String name;
    private String status;
    private String description;
    private String clusterName;
    private String type;
    private Long pvcName;
    private String AccessModes;
    private String storage;

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
        return AccessModes;
    }

    public void setAccessModes(String accessModes) {
        AccessModes = accessModes;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}
