package io.choerodon.devops.infra.dto;

import javax.annotation.Nullable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 主机配置
 *
 * @author zmf
 * @since 2020/9/14
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_host")
public class DevopsHostDTO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty("主机名称")
    private String name;

    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostType}
     */
    @ApiModelProperty("主机类型")
    private String type;

    @ApiModelProperty("项目id")
    private Long projectId;

    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostStatus}
     */
    @ApiModelProperty("主机状态")
    private String hostStatus;

    @Nullable
    @ApiModelProperty("主机连接错误信息")
    private String hostCheckError;

    @Nullable
    @ApiModelProperty("jmeter_check_error")
    private String jmeterCheckError;

    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostStatus}
     */
    @ApiModelProperty("jmeter状态")
    private String jmeterStatus;

    @ApiModelProperty("主机ip")
    private String hostIp;

    @ApiModelProperty("主机ssh的端口")
    private Integer sshPort;

    /**
     * {@link io.choerodon.devops.infra.enums.CdHostAccountType}
     */
    @ApiModelProperty("认证类型")
    private String authType;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码/rsa秘钥")
    private String password;

    @ApiModelProperty("jmeter进程的端口号")
    private Integer jmeterPort;

    @ApiModelProperty("jmeter二进制文件的路径")
    private String jmeterPath;

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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
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

    @Nullable
    public String getHostCheckError() {
        return hostCheckError;
    }

    public void setHostCheckError(@Nullable String hostCheckError) {
        this.hostCheckError = hostCheckError;
    }

    @Nullable
    public String getJmeterCheckError() {
        return jmeterCheckError;
    }

    public void setJmeterCheckError(@Nullable String jmeterCheckError) {
        this.jmeterCheckError = jmeterCheckError;
    }
}
