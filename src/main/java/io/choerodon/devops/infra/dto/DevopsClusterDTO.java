package io.choerodon.devops.infra.dto;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_cluster")
public class DevopsClusterDTO extends AuditDomain {

    public static final String ENCRYPT_KEY = "devops_cluster";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Encrypt(DevopsClusterDTO.ENCRYPT_KEY)
    private Long id;
    /**
     * 这个集群所属项目的组织id， 不能为空
     */
    private Long organizationId;
    private String type;
    private String status;
    private String name;
    private String code;
    private String description;
    private String token;
    private Boolean skipCheckProjectPermission;
    private String choerodonId;
    private String namespaces;
    private Boolean isInit;
    private Long createdBy;
    private Long projectId;
    private String podName;
    private String namespace;

    @ApiModelProperty("系统环境id, 用于部署集群相关的资源")
    private Long systemEnvId;

    private Long clientId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChoerodonId() {
        return choerodonId;
    }

    public void setChoerodonId(String choerodonId) {
        this.choerodonId = choerodonId;
    }

    public String getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(String namespaces) {
        this.namespaces = namespaces;
    }

    public Boolean getInit() {
        return isInit;
    }

    public void setInit(Boolean init) {
        isInit = init;
    }

    @Override
    public Long getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getSystemEnvId() {
        return systemEnvId;
    }

    public void setSystemEnvId(Long systemEnvId) {
        this.systemEnvId = systemEnvId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
