package io.choerodon.devops.api.vo;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.validator.annotation.AtLeastOneNotEmpty;
import io.choerodon.devops.api.validator.annotation.QuantityCheck;

@AtLeastOneNotEmpty(fields = {"pvId", "pvName"}, message = "error.pv.id.or.name.null")
public class DevopsPvcReqVO {
    @Encrypt
    @ApiModelProperty("PVC id")
    private Long id;

    @ApiModelProperty("PVC名称")
    @Pattern(regexp = "[a-z]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*", message = "error.pvc.name.pattern")
    @Length(max = 40, min = 1, message = "error.pvc.name.length")
    private String name;

    @Encrypt
    @NotNull(message = "error.env.id.null")
    @ApiModelProperty("PVC绑定环境ID")
    private Long envId;

    @Encrypt
    @ApiModelProperty("PVC绑定PV id")
    private Long pvId;

    @ApiModelProperty("PVC绑定PV的name")
    private String pvName;

    @Encrypt
    @ApiModelProperty("PVC绑定PV所在的集群id")
    private Long clusterId;

    @NotEmpty(message = "error.pvc.accessModes.empty")
    @ApiModelProperty("访问模式")
    private String accessModes;

    @QuantityCheck(message = "error.pvc.request.source.error")
    @ApiModelProperty("资源申请数量")
    private String requestResource;

    @ApiModelProperty(value = "本次请求的操作类型，create/update", hidden = true)
    private String commandType;

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getPvName() {
        return pvName;
    }

    public void setPvName(String pvName) {
        this.pvName = pvName;
    }

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
