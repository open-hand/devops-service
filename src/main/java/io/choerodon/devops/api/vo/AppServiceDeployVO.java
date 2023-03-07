package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.validator.annotation.AtLeastOneNotEmpty;

@AtLeastOneNotEmpty(fields = {"valueId", "values"}, message = "{devops.atleast.one.not.empty}")
public class AppServiceDeployVO {
    @Encrypt
    @ApiModelProperty("服务id/必填")
    @NotNull(message = "{devops.app.id.null}")
    private Long appServiceId;

    @Encrypt
    @ApiModelProperty("服务应用版本id/必填")
    @NotNull(message = "{devops.appversion.not.exist.in.database}")
    private Long appServiceVersionId;

    @Encrypt
    @ApiModelProperty("环境id/必填")
    @NotNull(message = "{devops.env.id.null}")
    private Long environmentId;

    @ApiModelProperty("部署配置")
    private String values;

    @Encrypt
    @ApiModelProperty("值id")
    private Long valueId;

    @ApiModelProperty("实例名称")
    private String instanceName;

    @Encrypt
    @ApiModelProperty("应用id")
    private Long appCenterId;

    @Encrypt
    @ApiModelProperty("实例id")
    private Long instanceId;

    @ApiModelProperty("操作类型")
    private String type;

    @Encrypt
    @ApiModelProperty("命令id")
    private Long commandId;

    @ApiModelProperty("是否改变")
    private boolean isNotChange;

    @Encrypt
    @ApiModelProperty("记录id")
    private Long recordId;
    @ApiModelProperty("应用中心应用名称")
    @Size(min = 1, max = 53, message = "{devops.env.app.center.name.length}")
    @NotBlank(message = "{devops.app.name.null}")
    private String appName;

    @ApiModelProperty("应用中心应用code，同时也作为实例名称")
    @Size(min = 1, max = 53, message = "{devops.env.app.center.code.length}")
    @NotBlank(message = "{devops.app.code.null}")
    private String appCode;

    @ApiModelProperty("服务来源")
    private String appServiceSource;

    @ApiModelProperty(value = "实例的网络配置")
    private DevopsServiceReqVO devopsServiceReqVO;
    @ApiModelProperty(value = "实例的域名配置")
    private DevopsIngressVO devopsIngressVO;

    @ApiModelProperty("replicas生效策略在 values/deployment")
    private  String replicasStrategy;

    public AppServiceDeployVO() {
    }

    public AppServiceDeployVO(Long appServiceId,
                              Long appServiceVersionId,
                              Long environmentId,
                              String values,
                              Long valueId,
                              String instanceName,
                              Long instanceId,
                              String type,
                              String appName,
                              String appCode) {
        this.appServiceId = appServiceId;
        this.appServiceVersionId = appServiceVersionId;
        this.environmentId = environmentId;
        this.values = values;
        this.valueId = valueId;
        this.instanceName = instanceName;
        this.instanceId = instanceId;
        this.type = type;
        this.appName = appName;
        this.appCode = appCode;
    }


    public String getReplicasStrategy() {
        return replicasStrategy;
    }

    public void setReplicasStrategy(String replicasStrategy) {
        this.replicasStrategy = replicasStrategy;
    }

    public Long getAppCenterId() {
        return appCenterId;
    }

    public void setAppCenterId(Long appCenterId) {
        this.appCenterId = appCenterId;
    }

    public boolean isNotChange() {
        return isNotChange;
    }

    public void setNotChange(boolean notChange) {
        isNotChange = notChange;
    }

    public String getAppServiceSource() {
        return appServiceSource;
    }

    public void setAppServiceSource(String appServiceSource) {
        this.appServiceSource = appServiceSource;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public boolean getIsNotChange() {
        return isNotChange;
    }

    public void setIsNotChange(boolean isNotChange) {
        this.isNotChange = isNotChange;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
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
}
