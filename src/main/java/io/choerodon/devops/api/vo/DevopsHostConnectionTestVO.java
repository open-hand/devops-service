package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Range;

import io.choerodon.devops.api.validator.annotation.EnumCheck;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.enums.DevopsHostType;
import io.choerodon.devops.infra.enums.HostAuthType;

/**
 * @author zmf
 * @since 2020/9/15
 */
public class DevopsHostConnectionTestVO {
    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostType}
     */
    @EnumCheck(message = "{devops.host.type.invalid}", enumClass = DevopsHostType.class)
    @ApiModelProperty("主机类型")
    private String type;

    @Pattern(regexp = GitOpsConstants.IP_PATTERN, message = "{devops.host.ip.invalid}")
    @ApiModelProperty("主机ip")
    private String hostIp;

    @Range(max = 65535, message = "{devops.ssh.port.invalid}")
    @ApiModelProperty("主机ssh的端口")
    private Integer sshPort;

    /**
     * {@link HostAuthType}
     */
    @EnumCheck(message = "{devops.host.auth.type.invalid}", enumClass = HostAuthType.class)
    @ApiModelProperty("认证类型")
    private String authType;

    @NotEmpty(message = "{devops.host.username.empty}")
    @ApiModelProperty("用户名")
    private String username;

    @NotEmpty(message = "{devops.host.password.empty}")
    @ApiModelProperty("密码/rsa秘钥")
    private String password;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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
