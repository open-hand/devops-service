package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsClusterBasicInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "集群名称")
    private String name;
    private String code;
    @ApiModelProperty(value = "是否已经连接")
    private Boolean connect;
    @ApiModelProperty(value = "节点列表")
    private List<String> nodes;

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

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
