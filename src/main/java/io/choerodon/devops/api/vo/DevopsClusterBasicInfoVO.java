package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class DevopsClusterBasicInfoVO {

    private Long id;
    @ApiModelProperty(value = "集群名称")
    private String name;
    private String code;
    @ApiModelProperty(value = "是否已经连接")
    private Boolean connect;
    @ApiModelProperty(value = "是否需要升级")
    private Boolean upgrade;
    @ApiModelProperty(value = "升级信息")
    private String upgradeMessage;
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

    public Boolean getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(Boolean upgrade) {
        this.upgrade = upgrade;
    }

    public String getUpgradeMessage() {
        return upgradeMessage;
    }

    public void setUpgradeMessage(String upgradeMessage) {
        this.upgradeMessage = upgradeMessage;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
