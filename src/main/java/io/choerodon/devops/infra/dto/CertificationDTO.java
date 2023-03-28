package io.choerodon.devops.infra.dto;

import java.util.Date;
import java.util.List;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.C7nCertificationCreateOrUpdateVO;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:51
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_certification")
public class CertificationDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long certificationFileId;
    /**
     * 项目层证书需要组织id
     */
    private Long organizationId;
    private String name;
    private Long envId;
    private String domains;
    private Long commandId;
    private String status;
    @ApiModelProperty("证书资源的API版本/环境中的证书资源需要这个字段")
    private String apiVersion;
    private Date validFrom;
    private Date validUntil;
    private Boolean skipCheckProjectPermission;
    private Long orgCertId;
    private Long projectId;
    private Boolean expireNotice;
    private Integer advanceDays;
    private String type;

    @Transient
    @ApiModelProperty("key文件内容")
    private String keyValue;
    @Transient
    @ApiModelProperty("cert文件内容")
    private String certValue;
    @Transient
    private String commandType;
    @Transient
    private String commandStatus;
    @Transient
    private String error;
    @Transient
    private List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects;


    public CertificationDTO() {
    }

    /**
     * Certification constructor
     *
     * @param name    Certification's name
     * @param envId   Certification's environment ID
     * @param domains Certification's domains json format
     * @param status  Certification's status
     */
    public CertificationDTO(String name, Long envId, String domains, String status) {
        this.name = name;
        this.envId = envId;
        this.domains = domains;
        this.status = status;
    }

    public CertificationDTO(Long id, String name, Long envId, String domains, String status, Long orgCertId) {
        this.id = id;
        this.name = name;
        this.envId = envId;
        this.domains = domains;
        this.status = status;
        this.orgCertId = orgCertId;
    }

    public CertificationDTO(Long id, String name, Long envId, String domains, String status, Long orgCertId, String type, Boolean expireNotice, Integer advanceDays, List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.envId = envId;
        this.domains = domains;
        this.status = status;
        this.orgCertId = orgCertId;
        this.expireNotice = expireNotice;
        this.advanceDays = advanceDays;
        this.notifyObjects = notifyObjects;
    }

    public CertificationDTO(Long id, String name, Long envId, String domains, String status, Long orgCertId, String type, Boolean expireNotice, Integer advanceDays, List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects, Long objectVersionNumber) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.envId = envId;
        this.domains = domains;
        this.status = status;
        this.orgCertId = orgCertId;
        this.expireNotice = expireNotice;
        this.advanceDays = advanceDays;
        this.notifyObjects = notifyObjects;
        this.setObjectVersionNumber(objectVersionNumber);
    }

    /**
     * Certification constructor
     *
     * @param name  Certification's name
     * @param envId Certification's enviroment ID
     */
    public CertificationDTO(String name, Long envId) {
        this.name = name;
        this.envId = envId;
    }

    public CertificationDTO(String name, Long envId, Long certId) {
        this.name = name;
        this.envId = envId;
        this.id = certId;
    }

    public void setValid(Date from, Date until) {
        this.setValidFrom(from);
        this.setValidUntil(until);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getExpireNotice() {
        return expireNotice;
    }

    public void setExpireNotice(Boolean expireNotice) {
        this.expireNotice = expireNotice;
    }

    public Integer getAdvanceDays() {
        return advanceDays;
    }

    public void setAdvanceDays(Integer advanceDays) {
        this.advanceDays = advanceDays;
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

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getCertValue() {
        return certValue;
    }

    public void setCertValue(String certValue) {
        this.certValue = certValue;
    }

    public List<C7nCertificationCreateOrUpdateVO.NotifyObject> getNotifyObjects() {
        return notifyObjects;
    }

    public void setNotifyObjects(List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects) {
        this.notifyObjects = notifyObjects;
    }
}
