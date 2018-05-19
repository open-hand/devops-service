package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by Zenger on 2018/4/14.
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_env_pod")
public class DevopsEnvPodDO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    private Long appInstanceId;
    private String name;
    private String ip;
    private String status;
    private Boolean isReady;
    private String resourceVersion;
    private String namespace;

    @Transient
    private String appName;
    @Transient
    private String appVersion;
    @Transient
    private String instanceCode;
    @Transient
    private String envCode;
    @Transient
    private String envName;

    public DevopsEnvPodDO(Long appInstanceId) {
        this.appInstanceId = appInstanceId;
    }

    public DevopsEnvPodDO() {

    }

    /**
     * Devops Pod 数据库对象
     */
    public DevopsEnvPodDO(Long appInstanceId, String name, String ip, String status, Boolean ready) {
        this.appInstanceId = appInstanceId;
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

    public Long getAppInstanceId() {
        return appInstanceId;
    }

    public void setAppInstanceId(Long appInstanceId) {
        this.appInstanceId = appInstanceId;
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

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
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
}
