package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

public class AppServiceDeployVO {
    @ApiModelProperty("服务id/必填")
    @NotNull(message = "error.app.id.null")
    private Long appServiceId;

    @ApiModelProperty("服务应用版本id/必填")
    @NotNull(message = "appversion.not.exist.in.database")
    private Long appServiceVersionId;

    @ApiModelProperty("环境id/必填")
    @NotNull(message = "error.env.id.null")
    private Long environmentId;

    @ApiModelProperty("部署配置")
    private String values;

    // TODO values和valueId二选一校验
    @ApiModelProperty("值id")
    private Long valueId;

    @ApiModelProperty("实例名称/必填")
    @NotBlank(message = "error.app.instance.name.null")
    private String instanceName;

    @ApiModelProperty("实例id")
    private Long instanceId;

    @ApiModelProperty("操作类型")
    private String type;

    @ApiModelProperty("命令id")
    private Long commandId;

    @ApiModelProperty("是否改变")
    private boolean isNotChange;

    @ApiModelProperty("记录id")
    private Long recordId;

    private DevopsServiceReqVO devopsServiceReqVO;
    private DevopsIngressVO devopsIngressVO;

    public AppServiceDeployVO() {
    }

    public AppServiceDeployVO(Long appServiceVersionId, Long environmentId, String values, Long appServiceId, String type, Long instanceId, String instanceName, Long recordId, Long valueId) {
        this.appServiceVersionId = appServiceVersionId;
        this.environmentId = environmentId;
        this.values = values;
        this.appServiceId = appServiceId;
        this.type = type;
        this.instanceId = instanceId;
        this.instanceName = instanceName;
        this.recordId = recordId;
        this.valueId = valueId;
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
}
