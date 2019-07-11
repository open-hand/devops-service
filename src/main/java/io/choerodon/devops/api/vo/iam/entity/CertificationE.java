package io.choerodon.devops.api.vo.iam.entity;

import java.util.Date;
import java.util.List;

/**
 * Created by n!Ck
 * Date: 2018/8/21
 * Time: 10:30
 * Description:
 */
public class CertificationE {
    private Long id;
    private Long organizationId;
    private String name;
    private DevopsEnvironmentE environmentE;
    private List<String> domains;
    private String status;
    private Long commandId;
    private Date validFrom;
    private Date validUntil;
    private String commandType;
    private String commandStatus;
    private Long certificationFileId;
    private Boolean skipCheckProjectPermission;
    private String error;
    private Long orgCertId;

    public CertificationE() {
    }

    public CertificationE(Long id, String name, DevopsEnvironmentE environmentE, List<String> domains, String status, Long orgCertId) {
        this.id = id;
        this.name = name;
        this.environmentE = environmentE;
        this.domains = domains;
        this.status = status;
        this.orgCertId = orgCertId;
    }

    /**
     * check weather cert is active on date
     *
     * @param date checkDate
     * @return true if cert is active, else false
     */
    public Boolean checkValidity(Date date) {
        return this.validFrom != null && this.validUntil != null
                && date.after(this.validFrom) && date.before(this.validUntil);
    }

    /**
     * check weather cert is active now
     *
     * @return true if cert is active, else false
     */
    public Boolean checkValidity() {
        return this.checkValidity(new Date());
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

    public DevopsEnvironmentE getEnvironmentE() {
        return environmentE;
    }

    public void setEnvironmentE(DevopsEnvironmentE environmentE) {
        this.environmentE = environmentE;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
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

    public Long getCertificationFileId() {
        return certificationFileId;
    }

    public void setCertificationFileId(Long certificationFileId) {
        this.certificationFileId = certificationFileId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public Long getOrgCertId() {
        return orgCertId;
    }

    public void setOrgCertId(Long orgCertId) {
        this.orgCertId = orgCertId;
    }
}
