package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.PortMapVO;

/**
 * Created by Zenger on 2018/4/13.
 */
public class DevopsServiceReqVO {
    @ApiModelProperty("环境ID / 必填")
    @NotNull(message = "error.env.id.null")
    private Long envId;

    @ApiModelProperty("应用服务ID / 选填，用于创建与应用服务关联关系")
    private Long appServiceId;

    @ApiModelProperty("网络名称 / 必填，长度1-30")
    @NotNull(message = "error.name.null")
    @Size(min = 1, max = 30, message = "error.service.name.size")
    private String name;

    @ApiModelProperty("配置类型 / 必填")
    @NotNull(message = "error.type.null")
    private String type;

    private String externalIp;

    @ApiModelProperty("端口数据 / 必填")
    @NotNull(message = "error.ports.null")
    private List<PortMapVO> ports;

    @ApiModelProperty("目标对象为Endpoints，相应的信息")
    private Map<String, List<EndPointPortVO>> endPoints;

    @ApiModelProperty("目标对象为实例，实例的Code")
    private List<String> instances;

    @ApiModelProperty("目标对象为标签，标签键值对")
    private Map<String, String> label;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public List<String> getInstances() {
        return instances;
    }

    public void setInstances(List<String> instances) {
        this.instances = instances;
    }

    public List<PortMapVO> getPorts() {
        return ports;
    }

    public void setPorts(List<PortMapVO> ports) {
        this.ports = ports;
    }

    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(Map<String, String> labels) {
        this.label = labels;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, List<EndPointPortVO>> getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(Map<String, List<EndPointPortVO>> endPoints) {
        this.endPoints = endPoints;
    }
}
