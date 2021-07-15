package io.choerodon.devops.api.vo;

import io.choerodon.devops.api.validator.annotation.EnumCheck;
import io.choerodon.devops.infra.enums.HostConnectionType;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author shanyu
 * @since 2021/7/15
 */
public class DevopsHostConnectionVO {
    @ApiModelProperty("主机ip")
    private String hostIp;

    @ApiModelProperty("主机ssh的端口")
    private Integer sshPort;

    @ApiModelProperty("认证类型")
    private String authType;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码/rsa秘钥")
    private String password;

    /**
     * {@link HostConnectionType}
     */
    @EnumCheck(message = "error.host.connect.type.invalid", enumClass = HostConnectionType.class)
    @ApiModelProperty("连接方式")
    private String connectionType;

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
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

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }
}
