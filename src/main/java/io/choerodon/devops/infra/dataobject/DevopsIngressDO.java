package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;
import java.util.Objects;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 14:30
 * Description:
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_ingress")
public class DevopsIngressDO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;
    private Long projectId;
    private Long envId;
    private Long commandId;
    private String name;
    private String domain;
    private Boolean isUsable;
    private String status;
    private Long certId;

    @Transient
    private String envName;
    @Transient
    private String namespace;
    @Transient
    private List<DevopsIngressPathDO> devopsIngressPathDOS;


    public DevopsIngressDO() {
    }

    public DevopsIngressDO(String name) {
        this.name = name;
    }

    /**
     * 构造函数
     */
    public DevopsIngressDO(Long projectId, Long envId, String domain, String name) {
        this.projectId = projectId;
        this.envId = envId;
        this.domain = domain;
        this.name = name;
    }

    public DevopsIngressDO(Long projectId) {
        this.projectId = projectId;
    }

    public DevopsIngressDO(Long projectId, String name) {
        this.projectId = projectId;
        this.name = name;
    }

    public DevopsIngressDO(String domain, Long projectId) {
        this.projectId = projectId;
        this.domain = domain;
    }


    /**
     * 构造函数
     */
    public DevopsIngressDO(Long id, Long projectId, Long envId, String domain, String name, String status) {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.envId = envId;
        this.domain = domain;
        this.status = status;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUsable() {
        return isUsable;
    }

    public void setUsable(Boolean usable) {
        isUsable = usable;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCertId() {
        return certId;
    }

    public void setCertId(Long certId) {
        this.certId = certId;
    }

    public List<DevopsIngressPathDO> getDevopsIngressPathDOS() {
        return devopsIngressPathDOS;
    }

    public void setDevopsIngressPathDOS(List<DevopsIngressPathDO> devopsIngressPathDOS) {
        this.devopsIngressPathDOS = devopsIngressPathDOS;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DevopsIngressDO that = (DevopsIngressDO) o;
        return Objects.equals(id, that.id)
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(envId, that.envId)
                && Objects.equals(name, that.name)
                && Objects.equals(status, that.status)
                && Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, envId, name, domain);
    }
}
