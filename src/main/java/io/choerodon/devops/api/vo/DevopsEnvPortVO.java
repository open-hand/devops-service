package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author lizongwei
 * @date 2019/7/2
 */
public class DevopsEnvPortVO {

    @ApiModelProperty("资源名称")
    private String resourceName;
    @ApiModelProperty("端口号名称")
    private String portName;
    @ApiModelProperty("端口号")
    private Integer portValue;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public Integer getPortValue() {
        return portValue;
    }

    public void setPortValue(Integer portValue) {
        this.portValue = portValue;
    }
}
