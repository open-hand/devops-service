package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/14 10:15
 */
@Table(name = "devops_host_app_instance_rel")
@ModifyAudit
@VersionAudit
public class DevopsHostAppInstanceRelDTO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;

    private Long projectId;

    @Encrypt
    @ApiModelProperty("操作主机id")
    private Long hostId;
    @Encrypt
    @ApiModelProperty("操作主机id")
    private Long appId;
    /**
     * {@link io.choerodon.devops.infra.enums.AppSourceType}
     */
    @ApiModelProperty("应用来源")
    private String appSource;
    @Encrypt
    @ApiModelProperty("实例id")
    private Long instanceId;

    @ApiModelProperty("实例类型")
    private String instanceType;

    public DevopsHostAppInstanceRelDTO() {
    }

    public DevopsHostAppInstanceRelDTO(Long projectId, Long hostId, Long appId, String appSource, Long instanceId, String instanceType) {
        this.projectId = projectId;
        this.hostId = hostId;
        this.appId = appId;
        this.appSource = appSource;
        this.instanceId = instanceId;
        this.instanceType = instanceType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAppSource() {
        return appSource;
    }

    public void setAppSource(String appSource) {
        this.appSource = appSource;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }
}
