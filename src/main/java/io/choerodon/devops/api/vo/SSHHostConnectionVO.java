package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author lihao
 * @date 2020/09/24
 */
public class SSHHostConnectionVO {
    @ApiModelProperty("主机地址")
    private String hostIp;

    @ApiModelProperty("主机port")
    private String sshPort;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码/秘钥")
    private String password;

    @ApiModelProperty("账号配置类型")
    private String authType;

    public SSHHostConnectionVO() {
    }

    public SSHHostConnectionVO(String hostIp, String sshPort, String username, String password, String authType) {
        this.hostIp = hostIp;
        this.sshPort = sshPort;
        this.username = username;
        this.password = password;
        this.authType = authType;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getSshPort() {
        return sshPort;
    }

    public void setSshPort(String sshPort) {
        this.sshPort = sshPort;
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

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }
}
