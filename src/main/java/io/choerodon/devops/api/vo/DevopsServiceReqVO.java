package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.validator.annotation.AtLeastOneNotEmpty;
import io.choerodon.devops.api.validator.annotation.AtMostSeveralFieldsNotEmpty;
import io.choerodon.devops.infra.dto.PortMapVO;

/**
 * Created by Zenger on 2018/4/13.
 */
@AtMostSeveralFieldsNotEmpty(message = "error.service.target.too.much",
        fields = {"endPoints", "selectors", "targetAppServiceId", "targetInstanceCode"})
@AtLeastOneNotEmpty(message = "error.service.target.null",
        fields = {"endPoints", "selectors", "targetAppServiceId", "targetInstanceCode"})
public class DevopsServiceReqVO {
    @ApiModelProperty("环境ID / 必填")
    @NotNull(message = "error.env.id.null")
    @Encrypt
    private Long envId;

    @Encrypt
    @ApiModelProperty("服务ID/从实例界面创建")
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

    @ApiModelProperty("目标对象为标签，标签键值对")
    private Map<String, String> selectors;

    @Encrypt
    @ApiModelProperty("目标对象是应用服务下所有实例时，应用服务的id")
    private Long targetAppServiceId;

    @ApiModelProperty("目标对象是单个实例时，实例code")
    private String targetInstanceCode;

    private DevopsIngressVO devopsIngressVO;

    public Map<String, String> getSelectors() {
        return selectors;
    }

    public void setSelectors(Map<String, String> selectors) {
        this.selectors = selectors;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
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

    public List<PortMapVO> getPorts() {
        return ports;
    }

    public void setPorts(List<PortMapVO> ports) {
        this.ports = ports;
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

    public DevopsIngressVO getDevopsIngressVO() {
        return devopsIngressVO;
    }

    public void setDevopsIngressVO(DevopsIngressVO devopsIngressVO) {
        this.devopsIngressVO = devopsIngressVO;
    }

    public Long getTargetAppServiceId() {
        return targetAppServiceId;
    }

    public void setTargetAppServiceId(Long targetAppServiceId) {
        this.targetAppServiceId = targetAppServiceId;
    }

    public String getTargetInstanceCode() {
        return targetInstanceCode;
    }

    public void setTargetInstanceCode(String targetInstanceCode) {
        this.targetInstanceCode = targetInstanceCode;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }
}
