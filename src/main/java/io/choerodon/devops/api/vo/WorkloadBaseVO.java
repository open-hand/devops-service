package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class WorkloadBaseVO {
    @ApiModelProperty("资源名称")
    private String name;
    @ApiModelProperty("资源所属环境id")
    private Long envId;
    @ApiModelProperty("资源")
    private String content;
    @ApiModelProperty("操作类型，新增/更新")
    private String operateType;
    @ApiModelProperty("资源id")
    private Long resourceId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }
}
