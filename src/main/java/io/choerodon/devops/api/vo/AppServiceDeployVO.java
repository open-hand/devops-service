package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.validator.annotation.AtLeastOneNotEmpty;

@AtLeastOneNotEmpty(fields = {"valueId", "values"}, message = "error.atleast.one.not.empty")
public class AppServiceDeployVO {
    @Encrypt
    @ApiModelProperty("服务id/必填")
    @NotNull(message = "error.app.id.null")
    private Long appServiceId;

    @Encrypt
    @ApiModelProperty("服务应用版本id/必填")
    @NotNull(message = "appversion.not.exist.in.database")
    private Long appServiceVersionId;

    @ApiModelProperty("环境id/必填")
    @NotNull(message = "error.env.id.null")
    private Long environmentId;

    @ApiModelProperty("部署配置")
    private String values;

    @Encrypt
    @ApiModelProperty("值id")
    private Long valueId;

    @ApiModelProperty("实例名称/必填")
    @NotBlank(message = "error.app.instance.name.null")
    private String instanceName;

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

    @Encrypt
    private Long deployInfoId;

    private DevopsServiceReqVO devopsServiceReqVO;
    private DevopsIngressVO devopsIngressVO;


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

    public Long getDeployInfoId() {
        return deployInfoId;
    }

    public void setDeployInfoId(Long deployInfoId) {
        this.deployInfoId = deployInfoId;
    }
}
