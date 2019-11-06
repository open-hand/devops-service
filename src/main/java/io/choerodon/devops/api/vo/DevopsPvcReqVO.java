package io.choerodon.devops.api.vo;


import io.choerodon.devops.api.validator.annotation.QuantityCheck;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class DevopsPvcReqVO {
    @ApiModelProperty("PVC id")
    private Long id;

    @ApiModelProperty("PVC名称")
    @Pattern(regexp = "[a-z]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*", message = "error.pvc.name.pattern")
    @Length(max = 40, min = 1, message = "error.pvc.name.length")
    private String name;

    @NotNull(message = "error.env.id.null")
    @ApiModelProperty("PVC绑定环境ID")
    private Long envId;

    @NotNull(message = "error.pvc.pv.id.null")
    @ApiModelProperty("PVC绑定PV id")
    private Long pvId;

    @NotEmpty(message = "error.pvc.accessModes.empty")
    @ApiModelProperty("访问模式")
    private String accessModes;

    @QuantityCheck(message = "error.pvc.request.source.error")
    @ApiModelProperty("资源申请数量")
    private String requestResource;

    @ApiModelProperty(value = "本次请求的操作类型，create/update", hidden = true)
    private String commandType;

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

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getPvId() {
        return pvId;
    }

    public void setPvId(Long pvId) {
        this.pvId = pvId;
    }

    public String getAccessModes() {
        return accessModes;
    }

    public void setAccessModes(String accessModes) {
        this.accessModes = accessModes;
    }

    public String getRequestResource() {
        return requestResource;
    }

    public void setRequestResource(String requestResource) {
        this.requestResource = requestResource;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}
