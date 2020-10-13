package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.enums.HostSourceEnum;

/**
 * @author scp
 * @date 2020/7/8
 * @description
 */
public class HostConnectionVO {
    @ApiModelProperty("主机Ip")
    private String hostIp;

    @ApiModelProperty("主机port")
    private String hostPort;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("密钥")
    private String accountKey;

    @ApiModelProperty("账号配置类型")
    private String accountType;


    @ApiModelProperty("主机id")
    @Encrypt
    private Long hostId;

    /**
     * {@link HostSourceEnum}
     */
    @ApiModelProperty("主机来源")
    private String hostSource;

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getHostSource() {
        return hostSource;
    }

    public void setHostSource(String hostSource) {
        this.hostSource = hostSource;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
