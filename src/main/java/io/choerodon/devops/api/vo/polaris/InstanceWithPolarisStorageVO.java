package io.choerodon.devops.api.vo.polaris;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/20/20
 */
public class InstanceWithPolarisStorageVO {
    @ApiModelProperty("实例id")
    private Long instanceId;
    @ApiModelProperty("实例code")
    private String instanceCode;
    @ApiModelProperty("应用服务名称")
    private String appServiceName;
    @ApiModelProperty("应用服务id")
    private Long appServiceId;
    @ApiModelProperty("应用服务code")
    private String appServiceCode;
    @ApiModelProperty("实例包含的配置文件")
    private List<PolarisStorageControllerResultVO> items;
    @ApiModelProperty("是否有error级别的检测项")
    private Boolean hasErrors;
    @ApiModelProperty("是否是检查的数据，存在数据库中的值都是true")
    private Boolean checked = Boolean.TRUE;

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceCode() {
        return instanceCode;
    }

    public void setInstanceCode(String instanceCode) {
        this.instanceCode = instanceCode;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public List<PolarisStorageControllerResultVO> getItems() {
        return items;
    }

    public void setItems(List<PolarisStorageControllerResultVO> items) {
        this.items = items;
    }

    public Boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
