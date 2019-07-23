package io.choerodon.devops.api.vo;

/**
 * @author lizongwei
 * @date 2019/7/2
 */
public class DevopsEnvPortVO {

    private String resourceName;

    private String portName;

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
