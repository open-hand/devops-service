package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Sheep on 2019/7/3.
 */
public class DevopsCustomizeResourceReqVO {


    @ApiModelProperty("环境id/必填")
    @NotNull(message = "error.env.id.null")
    private Long envId;
    @ApiModelProperty("资源id")
    private Long resourceId;
    @ApiModelProperty("操作类型")
    private String type;
    @ApiModelProperty("资源内容")
    private String content;


    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
