package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_middleware")
public class DevopsMiddlewareDTO extends AuditDomain {
    @Id
    @GeneratedValue
    @ApiModelProperty("自增主键")
    @Encrypt
    private Long id;

    @ApiModelProperty("项目id")
    private Long projectId;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("中间件类型")
    private String type;

    @ApiModelProperty("版本")
    private String version;

    @ApiModelProperty("部署模式")
    private String mode;

    @ApiModelProperty("主机ids")
    private String hostIds;

    @ApiModelProperty("配置内容")
    private String configuration;

    public DevopsMiddlewareDTO(Long projectId, String name, String type, String mode, String version, String hostIds, String configuration) {
        this.projectId = projectId;
        this.name = name;
        this.type = type;
        this.mode = mode;
        this.version = version;
        this.hostIds = hostIds;
        this.configuration = configuration;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getHostIds() {
        return hostIds;
    }

    public void setHostIds(String hostIds) {
        this.hostIds = hostIds;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
}