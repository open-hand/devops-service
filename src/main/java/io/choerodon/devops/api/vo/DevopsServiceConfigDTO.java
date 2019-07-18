package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.infra.dto.PortMapDTO;

/**
 * Creator: Runge
 * Date: 2018/8/3
 * Time: 13:40
 * Description:
 */
public class DevopsServiceConfigDTO {
    private List<String> externalIps;
    private List<PortMapDTO> ports;

    public List<String> getExternalIps() {
        return externalIps;
    }

    public void setExternalIps(List<String> externalIps) {
        this.externalIps = externalIps;
    }

    public List<PortMapDTO> getPorts() {
        return ports;
    }

    public void setPorts(List<PortMapDTO> ports) {
        this.ports = ports;
    }
}
