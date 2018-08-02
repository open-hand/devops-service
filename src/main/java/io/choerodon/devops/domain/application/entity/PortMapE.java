package io.choerodon.devops.domain.application.entity;

import java.util.Objects;

/**
 * Creator: Runge
 * Date: 2018/8/1
 * Time: 11:04
 * Description:
 */
public class PortMapE {
    private String name;
    private Long port;
    private Long nodePort;
    private String protocol;
    private Long targetPort;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNodePort() {
        return nodePort;
    }

    public void setNodePort(Long nodePort) {
        this.nodePort = nodePort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public Long getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(Long targetPort) {
        this.targetPort = targetPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PortMapE)) {
            return false;
        }
        PortMapE portMapE = (PortMapE) o;
        return Objects.equals(getPort(), portMapE.getPort())
                && Objects.equals(getNodePort(), portMapE.getNodePort())
                && Objects.equals(getProtocol(), portMapE.getProtocol())
                && Objects.equals(getTargetPort(), portMapE.getTargetPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPort(), getNodePort(), getProtocol(), getTargetPort());
    }
}
