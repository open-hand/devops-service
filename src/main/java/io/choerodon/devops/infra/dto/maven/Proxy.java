package io.choerodon.devops.infra.dto.maven;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-14
 */
public class Proxy {
    @ApiModelProperty("认证id")
    private String id;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("是否启用")
    private Boolean active = true;
    @ApiModelProperty("protocol")
    private String protocol;
    @ApiModelProperty("host")
    private String host;
    @ApiModelProperty("port")
    private Integer port;

    public Proxy() {
    }

    public Proxy(String id, String username, String password, Boolean active, String protocol, String host, Integer port) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.active = active;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return "Server{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
