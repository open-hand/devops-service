package io.choerodon.devops.api.vo;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 * @since 2020/12/14
 */
public class MarketInstanceCreationRequestVO {
    @Nullable
    @Encrypt
    @ApiModelProperty("实例id")
    private Long instanceId;

    @Encrypt
    private Long marketAppServiceId;

    @Encrypt
    @ApiModelProperty("市场市场服务版本id")
    private Long marketDeployObjectId;

    @ApiModelProperty("values内容")
    private String values;

    @Size(min = 1, max = 53, message = "error.app.instance.name.length")
    @NotBlank(message = "error.app.instance.name.null")
    @ApiModelProperty("实例的code")
    private String instanceName;

    /**
     * {@link io.choerodon.devops.infra.enums.CommandType}
     */
    @ApiModelProperty("操作类型")
    private String commandType;

    @Encrypt
    @ApiModelProperty("环境id")
    private Long environmentId;

    private DevopsServiceReqVO devopsServiceReqVO;
    private DevopsIngressVO devopsIngressVO;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Boolean notChanged;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Long commandId;

    @JsonIgnore
    @ApiModelProperty(value = "chart版本", hidden = true)
    private String chartVersion;

    @Nullable
    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(@Nullable Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getMarketDeployObjectId() {
        return marketDeployObjectId;
    }

    public void setMarketDeployObjectId(Long marketDeployObjectId) {
        this.marketDeployObjectId = marketDeployObjectId;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public DevopsServiceReqVO getDevopsServiceReqVO() {
        return devopsServiceReqVO;
    }

    public void setDevopsServiceReqVO(DevopsServiceReqVO devopsServiceReqVO) {
        this.devopsServiceReqVO = devopsServiceReqVO;
    }

    public DevopsIngressVO getDevopsIngressVO() {
        return devopsIngressVO;
    }

    public void setDevopsIngressVO(DevopsIngressVO devopsIngressVO) {
        this.devopsIngressVO = devopsIngressVO;
    }

    public Long getMarketAppServiceId() {
        return marketAppServiceId;
    }

    public void setMarketAppServiceId(Long marketAppServiceId) {
        this.marketAppServiceId = marketAppServiceId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Boolean getNotChanged() {
        return notChanged;
    }

    public void setNotChanged(Boolean notChanged) {
        this.notChanged = notChanged;
    }

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }
}
