package io.choerodon.devops.infra.dataobject;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.choerodon.mybatis.annotation.ModifyAudit;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:51
 * Description:
 */
@ModifyAudit
@Table(name = "devops_certification")
public class CertificationDO {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private Long envId;
    private String domains;
    private Long commandId;
    private String status;
    private Date validFrom;
    private Date validUntil;

    @Transient
    private String commandType;
    @Transient
    private String commandStatus;
    @Transient
    private String error;

    public CertificationDO() {
    }

    /**
     * Certification constructor
     *
     * @param name    Certification's name
     * @param envId   Certification's enviroment ID
     * @param domains Certification's domains json format
     * @param status  Certification's status
     */
    public CertificationDO(String name, Long envId, String domains, String status) {
        this.name = name;
        this.envId = envId;
        this.domains = domains;
        this.status = status;
    }

    /**
     * Certification constructor
     *
     * @param name  Certification's name
     * @param envId Certification's enviroment ID
     */
    public CertificationDO(String name, Long envId) {
        this.name = name;
        this.envId = envId;
    }

    public void setValid(Date from, Date until) {
        this.setValidFrom(from);
        this.setValidUntil(until);
    }

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

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getDomains() {
        return domains;
    }

    public void setDomains(String domains) {
        this.domains = domains;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
