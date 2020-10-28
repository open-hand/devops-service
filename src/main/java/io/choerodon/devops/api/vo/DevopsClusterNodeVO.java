package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.enums.ClusterNodeRoleEnum;

public class DevopsClusterNodeVO {
    @Encrypt
    private Long id;

    @ApiModelProperty("节点名称")
    private String name;

    /**
     * {@link ClusterNodeRoleEnum}
     */
    @ApiModelProperty("节点类型")
    private Integer role;

    @ApiModelProperty("项目id")
    private Long projectId;

    @ApiModelProperty("节点ip")
    private String hostIp;

    @ApiModelProperty("节点ssh的端口")
    private Integer hostPort;

    /**
     * {@link io.choerodon.devops.infra.enums.HostAuthType}
     */
    @ApiModelProperty("认证类型")
    private String authType;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码/rsa秘钥")
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

    @Override
    public String toString() {
        return "DevopsClusterNodeDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + role + '\'' +
                ", projectId=" + projectId +
                ", hostIp='" + hostIp + '\'' +
                ", sshPort=" + hostPort +
                ", authType='" + authType + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
