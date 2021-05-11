package io.choerodon.devops.api.vo;

import java.util.Date;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.HostAuthType;

/**
 * 主机配置
 *
 * @author zmf
 * @since 2020/9/14
 */
public class DevopsHostVO {
    @Encrypt
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("主机名称")
    private String name;

    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostType}
     */
    @ApiModelProperty("主机类型")
    private String type;

    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostStatus}
     */
    @ApiModelProperty("主机状态")
    private String hostStatus;

    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostStatus}
     */
    @Deprecated
    @ApiModelProperty("jmeter状态")
    private String jmeterStatus;

    @Nullable
    @ApiModelProperty("主机连接错误信息")
    private String hostCheckError;

    @Deprecated
    @Nullable
    @ApiModelProperty("jmeter连接错误信息")
    private String jmeterCheckError;

    @ApiModelProperty("主机ip")
    private String hostIp;

    @ApiModelProperty("主机ssh的端口")
    private Integer sshPort;

    @ApiModelProperty("内网ip")
    private String privateIp;

    @ApiModelProperty("内网ssh端口")
    private Integer privatePort;

    /**
     * {@link HostAuthType}
     */
    @ApiModelProperty("认证类型")
    private String authType;

    @ApiModelProperty("用户名")
    private String username;

    @Deprecated
    @ApiModelProperty("jmeter进程的端口号")
    private Integer jmeterPort;

    @Deprecated
    @ApiModelProperty("jmeter二进制文件的路径")
    private String jmeterPath;

    @ApiModelProperty("更新者信息")
    private IamUserDTO updaterInfo;

    @ApiModelProperty("最后更新时间")
    private Date lastUpdateDate;

    @JsonIgnore
    private Long lastUpdatedBy;

    private Boolean selected;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(String hostStatus) {
        this.hostStatus = hostStatus;
    }

    @Deprecated
    public String getJmeterStatus() {
        return jmeterStatus;
    }

    @Deprecated
    public void setJmeterStatus(String jmeterStatus) {
        this.jmeterStatus = jmeterStatus;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
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

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
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

    public IamUserDTO getUpdaterInfo() {
        return updaterInfo;
    }

    public void setUpdaterInfo(IamUserDTO updaterInfo) {
        this.updaterInfo = updaterInfo;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Nullable
    public String getHostCheckError() {
        return hostCheckError;
    }

    public void setHostCheckError(@Nullable String hostCheckError) {
        this.hostCheckError = hostCheckError;
    }

    @Deprecated
    @Nullable
    public String getJmeterCheckError() {
        return jmeterCheckError;
    }
    @Deprecated
    public void setJmeterCheckError(@Nullable String jmeterCheckError) {
        this.jmeterCheckError = jmeterCheckError;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
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
