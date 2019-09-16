package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author lihao
 * @date 2019-09-15 15:43
 */
public class DevopsCustomizeResourceUpdateVO {
    @ApiModelProperty("环境id")
    @NotNull(message = "error.env.id.null")
    private Long envId;
    @ApiModelProperty("资源id")
    @NotNull(message = "error.customize.resource.id.null")
    private Long resourceId;
    @ApiModelProperty("操作类型")
    private String type;
    @ApiModelProperty("资源内容")
    @NotBlank(message = "error.customize.resource.content.null")
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
