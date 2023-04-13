package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsClusterBasicInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "集群名称")
    private String name;
    @ApiModelProperty(value = "集群编码")
    private String code;
    @ApiModelProperty(value = "是否已经连接")
    private Boolean connect;
    @ApiModelProperty(value = "集群状态")
    private String status;
    @ApiModelProperty(value = "节点列表")
    private List<String> nodes;
    @ApiModelProperty(value = "集群类型")
    private String type;
    @ApiModelProperty(value = "集群安装错误信息")
    private String errorMessage;
    @ApiModelProperty(value = "agent的pod名称")
    private String podName;
    @ApiModelProperty(value = "agent所在集群的命名空间")
    private String namespace;

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public String getStatus() {
        return status;
    }

    public DevopsClusterBasicInfoVO setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getType() {
        return type;
    }

    public DevopsClusterBasicInfoVO setType(String type) {
        this.type = type;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }
}
