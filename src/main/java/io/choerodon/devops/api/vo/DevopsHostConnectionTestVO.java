package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Range;

import io.choerodon.devops.api.validator.annotation.EnumCheck;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.enums.CdHostAccountType;
import io.choerodon.devops.infra.enums.DevopsHostType;

/**
 * @author zmf
 * @since 2020/9/15
 */
public class DevopsHostConnectionTestVO {
    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostType}
     */
    @EnumCheck(message = "error.host.type.invalid", enumClass = DevopsHostType.class)
    @ApiModelProperty("主机类型")
    private String type;

    @Pattern(regexp = GitOpsConstants.IP_PATTERN, message = "error.host.ip.invalid")
    @ApiModelProperty("主机ip")
    private String hostIp;

    @Range(max = 65535, message = "error.ssh.port.invalid")
    @ApiModelProperty("主机ssh的端口")
    private Integer sshPort;

    /**
     * {@link io.choerodon.devops.infra.enums.CdHostAccountType}
     */
    @EnumCheck(message = "error.host.auth.type.invalid", enumClass = CdHostAccountType.class)
    @ApiModelProperty("认证类型")
    private String authType;

    @NotEmpty(message = "error.host.username.empty")
    @ApiModelProperty("用户名")
    private String username;

    @NotEmpty(message = "error.host.password.empty")
    @ApiModelProperty("密码/rsa秘钥")
    private String password;

    @ApiModelProperty("jmeter进程的端口号")
    private Integer jmeterPort;

    @ApiModelProperty("jmeter的home目录")
    private String jmeterPath;

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

    public Integer getJmeterPort() {
        return jmeterPort;
    }

    public void setJmeterPort(Integer jmeterPort) {
        this.jmeterPort = jmeterPort;
    }

    public String getJmeterPath() {
        return jmeterPath;
    }

    public void setJmeterPath(String jmeterPath) {
        this.jmeterPath = jmeterPath;
    }
}
