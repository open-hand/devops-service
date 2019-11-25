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

    @ApiModelProperty("资源的状态")
    private String resourceStatus;

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
}
