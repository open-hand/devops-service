package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

public class WorkloadBaseCreateOrUpdateVO {
    @ApiModelProperty("环境id/必填")
    @NotNull(message = "error.env.id.null")
    private String envId;
    @ApiModelProperty("资源")
    private String content;
    @ApiModelProperty("操作类型，新增/更新")
    private String operateType;
    @ApiModelProperty("资源id")
    private String resourceId;

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
