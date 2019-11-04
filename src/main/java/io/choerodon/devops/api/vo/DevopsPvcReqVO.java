package io.choerodon.devops.api.vo;


import io.choerodon.devops.api.validator.annotation.QuantityCheck;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;

public class DevopsPvcReqVO {
    @ApiModelProperty("PVC id")
    private Long id;

    @ApiModelProperty("PVC名称")
    @Pattern(regexp = "[a-z]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*", message = "error.pvc.name.pattern")
    @Max(value = 40, message = "error.pvc.name.length.max")
    @Min(value = 10, message = "error.pvc.name.length.min")
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

    @NotEmpty(message = "error.pvc.type.empty")
    @ApiModelProperty("卷类型")
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
