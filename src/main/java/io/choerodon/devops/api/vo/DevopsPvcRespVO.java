package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.DevopsPvcDTO;

public class DevopsPvcRespVO extends DevopsResourceDataInfoVO {
    @ApiModelProperty("PVC id")
//    @Encrypt(DevopsPvcDTO.ENCRYPT_KEY)
    private Long id;

    @ApiModelProperty("PVC名称")
    private String name;

    @ApiModelProperty("PVC绑定环境ID")
    private Long envId;

    @ApiModelProperty("PVC绑定PV id")
    private Long pvId;

    @ApiModelProperty("访问模式")
    private String accessModes;

    @ApiModelProperty("资源申请数量")
    private String requestResource;

    @ApiModelProperty("绑定的PV类型")
    private String type;

    @ApiModelProperty("绑定的PV名称")
    private String pvName;

    @ApiModelProperty("PVC状态")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPvName() {
        return pvName;
    }

    public void setPvName(String pvName) {
        this.pvName = pvName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
