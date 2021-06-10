package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by younger on 2018/4/24.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_env_resource")
public class DevopsEnvResourceDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long instanceId;
    private Long resourceDetailId;
    private String kind;
    private String name;
    private Long envId;
    private Long commandId;
    private Long weight;
    private Long reversion;

    private String ownerRefKind;

    private String ownerRefName;

    @Transient
    private Long devopsEnvCommandId;
    @Transient
    private Long devopsEnvironmentId;

    public String getOwnerRefKind() {
        return ownerRefKind;
    }

    public void setOwnerRefKind(String ownerRefKind) {
        this.ownerRefKind = ownerRefKind;
    }

    public String getOwnerRefName() {
        return ownerRefName;
    }

    public void setOwnerRefName(String ownerRefName) {
        this.ownerRefName = ownerRefName;
    }

    public Long getDevopsEnvCommandId() {
        return devopsEnvCommandId;
    }

    public void setDevopsEnvCommandId(Long devopsEnvCommandId) {
        this.devopsEnvCommandId = devopsEnvCommandId;
    }

    public Long getDevopsEnvironmentId() {
        return devopsEnvironmentId;
    }

    public void setDevopsEnvironmentId(Long devopsEnvironmentId) {
        this.devopsEnvironmentId = devopsEnvironmentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getResourceDetailId() {
        return resourceDetailId;
    }

    public void setResourceDetailId(Long resourceDetailId) {
        this.resourceDetailId = resourceDetailId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Long getReversion() {
        return reversion;
    }

    public void setReversion(Long reversion) {
        this.reversion = reversion;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
}
