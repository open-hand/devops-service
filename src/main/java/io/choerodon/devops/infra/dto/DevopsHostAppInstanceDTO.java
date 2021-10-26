package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/3 11:48
 */
@Table(name = "devops_host_app_instance")
@ModifyAudit
@VersionAudit
public class DevopsHostAppInstanceDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("主机id")
    private Long hostId;
    @ApiModelProperty("应用id")
    private Long appId;
    @ApiModelProperty("实例编码")
    private String code;
    @ApiModelProperty("进程id")
    private String pid;
    @ApiModelProperty("占用端口")
    private String ports;
    @ApiModelProperty("部署来源")
    private String sourceType;
    @ApiModelProperty("进程状态")
    private String status;
    @ApiModelProperty("来源配置")
    private String sourceConfig;
    @ApiModelProperty("前置命令")
    private String preCommand;
    @ApiModelProperty("运行命令")
    private String runCommand;
    @ApiModelProperty("后置命令")
    private String postCommand;
    @ApiModelProperty("groupId")
    private String groupId;
    @ApiModelProperty("artifactId")
    private String artifactId;
    @ApiModelProperty("version")
    private String version;

    public DevopsHostAppInstanceDTO() {
    }

    public DevopsHostAppInstanceDTO(Long projectId, Long hostId, Long appId, String code, String sourceType, String sourceConfig, String preCommand, String runCommand, String postCommand) {
        this.projectId = projectId;
        this.hostId = hostId;
        this.appId = appId;
        this.code = code;
        this.sourceType = sourceType;
        this.sourceConfig = sourceConfig;
        this.preCommand = preCommand;
        this.runCommand = runCommand;
        this.postCommand = postCommand;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(String sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public String getPreCommand() {
        return preCommand;
    }

    public void setPreCommand(String preCommand) {
        this.preCommand = preCommand;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }

    public String getPostCommand() {
        return postCommand;
    }

    public void setPostCommand(String postCommand) {
        this.postCommand = postCommand;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
