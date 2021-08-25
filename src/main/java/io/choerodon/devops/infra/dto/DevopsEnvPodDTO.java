package io.choerodon.devops.infra.dto;

import java.util.Date;
import java.util.List;
import javax.persistence.*;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.ContainerVO;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by Zenger on 2018/4/14.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_env_pod")
public class DevopsEnvPodDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Encrypt
    private Long id;
    private Long instanceId;
    private String name;
    private String ip;
    private String status;
    private Boolean isReady;
    private String resourceVersion;
    private String namespace;
    private String nodeName;
    private Long restartCount;

    private String ownerRefKind;

    private String ownerRefName;

    private Long envId;

    @Transient
    private String appServiceName;
    @Transient
    private String publishLevel;
    @Transient
    private String appServiceVersion;
    @Transient
    private String instanceCode;
    @Transient
    private String envCode;
    @Transient
    private String envName;
    @Transient
    private Long projectId;
    @Transient
    private List<ContainerVO> containers;
    @Transient
    private String message;
    @Transient
    private Date creationDate;
    @Transient
    private Long clusterId;

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

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ContainerVO> getContainers() {
        return containers;
    }

    public void setContainers(List<ContainerVO> containers) {
        this.containers = containers;
    }

    public DevopsEnvPodDTO(Long instanceId) {
        this.instanceId = instanceId;
    }

    public DevopsEnvPodDTO() {

    }

    /**
     * Devops Pod 数据库对象
     */
    public DevopsEnvPodDTO(Long instanceId, String name, String ip, String status, Boolean ready) {
        this.instanceId = instanceId;
        this.name = name;
        this.ip = ip;
        this.status = status;
        this.isReady = ready;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceVersion() {
        return appServiceVersion;
    }

    public void setAppServiceVersion(String appServiceVersion) {
        this.appServiceVersion = appServiceVersion;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getInstanceCode() {
        return instanceCode;
    }

    public void setInstanceCode(String instanceCode) {
        this.instanceCode = instanceCode;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getPublishLevel() {
        return publishLevel;
    }

    public void setPublishLevel(String publishLevel) {
        this.publishLevel = publishLevel;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Long getRestartCount() {
        return restartCount;
    }

    public void setRestartCount(Long restartCount) {
        this.restartCount = restartCount;
    }
}
