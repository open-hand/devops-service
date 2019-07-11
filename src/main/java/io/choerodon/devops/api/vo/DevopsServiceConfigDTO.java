package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.domain.application.entity.PortMapE;

/**
 * Creator: Runge
 * Date: 2018/8/3
 * Time: 13:40
 * Description:
 */
public class DevopsServiceConfigDTO {
    private List<String> externalIps;
    private List<PortMapE> ports;

    public List<String> getExternalIps() {
        return externalIps;
    }

    public void setExternalIps(List<String> externalIps) {
        this.externalIps = externalIps;
    }

    public List<PortMapE> getPorts() {
        return ports;
    }

    public void setPorts(List<PortMapE> ports) {
        this.ports = ports;
    }
}
