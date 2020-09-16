package io.choerodon.devops.api.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 主机配置
 *
 * @author zmf
 * @since 2020/9/14
 */
public class DevopsHostVO {
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
    @ApiModelProperty("jmeter状态")
    private String jmeterStatus;

    @ApiModelProperty("主机ip")
    private String hostIp;

    @ApiModelProperty("主机ssh的端口")
    private String sshPort;

    /**
     * {@link io.choerodon.devops.infra.enums.CdHostAccountType}
     */
    @ApiModelProperty("认证类型")
    private String authType;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("jmeter进程的端口号")
    private String jmeterPort;

    @ApiModelProperty("jmeter二进制文件的路径")
    private String jmeterPath;

    @ApiModelProperty("更新者信息")
    private IamUserDTO updaterInfo;

    @ApiModelProperty("最后更新时间")
    private Date lastUpdateDate;

    @JsonIgnore
    private Long lastUpdatedBy;

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

    public String getJmeterStatus() {
        return jmeterStatus;
    }

    public void setJmeterStatus(String jmeterStatus) {
        this.jmeterStatus = jmeterStatus;
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

    public String getJmeterPort() {
        return jmeterPort;
    }

    public void setJmeterPort(String jmeterPort) {
        this.jmeterPort = jmeterPort;
    }

    public String getJmeterPath() {
        return jmeterPath;
    }

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
}
