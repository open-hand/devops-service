package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Range;

import io.choerodon.devops.api.validator.annotation.EnumCheck;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.enums.HostAuthType;

/**
 * @author zmf
 * @since 2020/9/15
 */
public class DevopsHostUpdateRequestVO {
    @NotEmpty(message = "error.host.name.empty")
    @Size(max = 30, message = "error.host.name.too.long")
    @ApiModelProperty("主机名称")
    private String name;

    @Pattern(regexp = GitOpsConstants.IP_PATTERN, message = "error.host.ip.invalid")
    @ApiModelProperty("主机ip")
    private String hostIp;

    @Range(max = 65535, message = "error.ssh.port.invalid")
    @ApiModelProperty("主机ssh的端口")
    private Integer sshPort;

    /**
     * {@link HostAuthType}
     */
    @EnumCheck(message = "error.host.auth.type.invalid", enumClass = HostAuthType.class)
    @ApiModelProperty("认证类型")
    private String authType;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码/rsa秘钥")
    private String password;

    @Deprecated
    @ApiModelProperty("jmeter进程的端口号")
    private Integer jmeterPort;

    @Deprecated
    @ApiModelProperty("jmeter二进制文件的路径")
    private String jmeterPath;

    @ApiModelProperty("内网ip")
    private String privateIp;

    @ApiModelProperty("内网ssh端口")
    private Integer privatePort;

    public String getName() {
        return name;
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

    @Deprecated
    public Integer getJmeterPort() {
        return jmeterPort;
    }

    @Deprecated
    public void setJmeterPort(Integer jmeterPort) {
        this.jmeterPort = jmeterPort;
    }

    @Deprecated
    public String getJmeterPath() {
        return jmeterPath;
    }

    @Deprecated
    public void setJmeterPath(String jmeterPath) {
        this.jmeterPath = jmeterPath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public Integer getPrivatePort() {
        return privatePort;
    }

    public void setPrivatePort(Integer privatePort) {
        this.privatePort = privatePort;
    }
}
