package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.enums.ClusterNodeRoleEnum;

public class DevopsClusterNodeVO {
    @Encrypt
    private Long id;

    @ApiModelProperty("节点名称")
    @NotNull(message = "error.node.name.is.null")
    private String name;

    @ApiModelProperty("节点类型，作为连接介质节点提供公网ip或者集群节点")
    @NotNull(message = "error.node.type.is.null")
    private String type;

    @ApiModelProperty("既作为外部节点，又作为内部节点，这个字段表示作为的内部节点的名称")
    private String innerNodeName;

    /**
     * {@link ClusterNodeRoleEnum}
     */
    @ApiModelProperty("节点角色")
    @NotNull(message = "error.node.role.is.null")
    private Integer role;

    @ApiModelProperty("项目id")
    private Long projectId;

    @ApiModelProperty("节点ip")
    @NotNull(message = "error.node.hostIp.is.null")
    private String hostIp;

    @ApiModelProperty("节点ssh的端口")
    @NotNull(message = "error.node.hostPort.is.null")
    private Integer hostPort;

    /**
     * {@link io.choerodon.devops.infra.enums.HostAuthType}
     */
    @ApiModelProperty("认证类型")
    @NotNull(message = "error.node.authType.is.null")
    private String authType;

    @ApiModelProperty("用户名")
    @NotNull(message = "error.node.username.is.null")
    private String username;

    @ApiModelProperty("密码/rsa秘钥")
    @NotNull(message = "error.node.password.is.null")
    private String password;

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

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public DevopsClusterNodeVO setType(String type) {
        this.type = type;
        return this;
    }

    public String getInnerNodeName() {
        return innerNodeName;
    }

    public void setInnerNodeName(String innerNodeName) {
        this.innerNodeName = innerNodeName;
    }

    @Override
    public String toString() {
        return "DevopsClusterNodeVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", innerNodeName='" + innerNodeName + '\'' +
                ", role=" + role +
                ", projectId=" + projectId +
                ", hostIp='" + hostIp + '\'' +
                ", hostPort=" + hostPort +
                ", authType='" + authType + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
