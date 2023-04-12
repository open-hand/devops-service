package io.choerodon.devops.api.vo.kubernetes;

import io.swagger.annotations.ApiModelProperty;

public class Command {
    @ApiModelProperty("Command的id")
    private Long id;

    @ApiModelProperty("资源类型")
    private String resourceType;

    @ApiModelProperty("资源名称")
    private String resourceName;

    @ApiModelProperty("这个command对应的git sha值")
    private String commit;

    @ApiModelProperty("资源的状态/如果资源需要状态，由agent返回")
    private String resourceStatus;

    @ApiModelProperty("资源所在namespace")
    private String namespace;

    private String payload;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(String resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return "Command{" +
                "id=" + id +
                ", resourceType='" + resourceType + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", commit='" + commit + '\'' +
                '}';
    }
}
