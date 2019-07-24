package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.infra.dto.PortMapVO;

/**
 * Creator: Runge
 * Date: 2018/8/3
 * Time: 13:40
 * Description:
 */
public class DevopsServiceConfigVO {
    private List<String> externalIps;
    private List<PortMapVO> ports;

    public List<String> getExternalIps() {
        return externalIps;
    }

    public void setExternalIps(List<String> externalIps) {
        this.externalIps = externalIps;
    }

    public List<PortMapVO> getPorts() {
        return ports;
    }

    public void setPorts(List<PortMapVO> ports) {
        this.ports = ports;
    }
}
