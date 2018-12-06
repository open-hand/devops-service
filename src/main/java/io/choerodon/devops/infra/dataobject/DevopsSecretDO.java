package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:08
 * Description:
 */

@ModifyAudit
@VersionAudit
@Table(name = "devops_secret")
public class DevopsSecretDO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    private Long envId;
    private String name;
    private String description;
    private String secretMaps;
    private Long commandId;

    @Transient
    private String status;

    public DevopsSecretDO() {
    }

    public DevopsSecretDO(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSecretMaps() {
        return secretMaps;
    }

    public void setSecretMaps(String secretMaps) {
        this.secretMaps = secretMaps;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
