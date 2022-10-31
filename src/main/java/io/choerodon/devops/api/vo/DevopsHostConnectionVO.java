package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.validator.annotation.EnumCheck;
import io.choerodon.devops.infra.enums.HostAuthType;

/**
 * @author shanyu
 * @since 2021/7/15
 */
public class DevopsHostConnectionVO {
    @ApiModelProperty("主机ip")
    private String hostIp;

    @ApiModelProperty("主机ssh的端口")
    private Integer sshPort;

    /**
     * {@link HostAuthType}
     */
    @EnumCheck(message = "{devops.host.auth.type.invalid}", enumClass = HostAuthType.class, skipNull = true)
    @ApiModelProperty("认证类型")
    private String authType;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码/rsa秘钥")
    private String password;

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
}
