package io.choerodon.devops.infra.dto;

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * Creator: Runge
 * Date: 2018/8/1
 * Time: 11:04
 * Description:
 */
public class PortMapVO implements Comparable<PortMapVO> {
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("端口号")
    private Integer port;
    @ApiModelProperty("nodeport")
    private Integer nodePort;
    @ApiModelProperty("协议")
    private String protocol;
    @ApiModelProperty("目标端口")
    private String targetPort;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PortMapVO)) {
            return false;
        }
        PortMapVO portMapVO = (PortMapVO) o;
        return Objects.equals(getPort(), portMapVO.getPort())
                && Objects.equals(getNodePort(), portMapVO.getNodePort())
                && Objects.equals(getProtocol(), portMapVO.getProtocol())
                && Objects.equals(getTargetPort(), portMapVO.getTargetPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPort(), getNodePort(), getProtocol(), getTargetPort());
    }

    @Override
    public int compareTo(PortMapVO o) {
        Integer portCompare = port.compareTo(o.port);
        if (portCompare != 0) {
            return portCompare;
        }
        Integer targetPortCompare = targetPort.compareTo(o.targetPort);
        if (targetPortCompare != 0) {
            return targetPortCompare;
        }
        Integer nodePortCompare = nodePort.compareTo(o.nodePort);
        return nodePortCompare != 0 ? nodePortCompare : protocol.compareTo(o.protocol);
    }
}
