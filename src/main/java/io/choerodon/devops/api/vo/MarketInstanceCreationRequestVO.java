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
    @ApiModelProperty("市场服务的应用Id")
    private Long marketAppServiceId;

    @Encrypt
    @ApiModelProperty("市场市场服务版本id")
    private Long marketDeployObjectId;

    @ApiModelProperty("应用类型:market hzero")
    private String applicationType;

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

    /**
     * 中间件不支持这两个字段 需设置为null
     */
    @ApiModelProperty("网络配置")
    private DevopsServiceReqVO devopsServiceReqVO;
    @ApiModelProperty("域名配置")
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
    @ApiModelProperty("中间件或者普通的市场应用")
    private String source;

    @ApiModelProperty("应用中心应用名称")
    @Size(min = 1, max = 53, message = "error.env.app.center.name.length")
    @NotBlank(message = "error.app.instance.name.null")
    private String appName;

    @ApiModelProperty("应用中心应用code，同时也作为实例名称")
    @Size(min = 1, max = 53, message = "error.env.app.center.code.length")
    @NotBlank(message = "error.app.instance.code.null")
    private String appCode;

    @JsonIgnore
    @ApiModelProperty("操作类型 hzero/base_component/create_app")
    private String operationType;

    public MarketInstanceCreationRequestVO() {
    }

    public MarketInstanceCreationRequestVO(@Nullable Long instanceId,
                                           Long marketAppServiceId,
                                           Long marketDeployObjectId,
                                           String values,
                                           String appName,
                                           String appCode,
                                           String commandType,
                                           Long environmentId,
                                           DevopsServiceReqVO devopsServiceReqVO,
                                           DevopsIngressVO devopsIngressVO,
                                           String source,
                                           String applicationType,
                                           String operationType) {
        this.instanceId = instanceId;
        this.marketAppServiceId = marketAppServiceId;
        this.marketDeployObjectId = marketDeployObjectId;
        this.values = values;
        this.appName = appName;
        this.appCode = appCode;
        this.commandType = commandType;
        this.environmentId = environmentId;
        this.devopsServiceReqVO = devopsServiceReqVO;
        this.devopsIngressVO = devopsIngressVO;
        this.source = source;
        this.applicationType = applicationType;
        this.operationType=operationType;
    }


    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
}
