package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/8 15:28
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_cd_env_deploy_info")
public class DevopsCdEnvDeployInfoDTO extends AuditDomain {
    @Encrypt
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Encrypt
    private Long appServiceId;

    @Encrypt
    private Long envId;

    @Encrypt
    private Long valueId;

    private Long projectId;
    private String deployType;  // 部署类型：新建实例 create 替换实例 update

    @Encrypt
    private Long instanceId;    // 替换实例时需要

    private String instanceName;    // 新建实例时需要

    @Encrypt
    private Long cdJobId; //cd 主机部署jar 部署id

    private String jarName; // jar 名称

    private Boolean checkEnvPermissionFlag;     // 部署时是否校验环境权限

    private Long appId;

    private String appName;

    private String appCode;

    private Boolean skipCheckPermission;

    private String appConfigJson;

    private String containerConfigJson;

    public String getAppConfigJson() {
        return appConfigJson;
    }

    public void setAppConfigJson(String appConfigJson) {
        this.appConfigJson = appConfigJson;
    }

    public String getContainerConfigJson() {
        return containerConfigJson;
    }

    public void setContainerConfigJson(String containerConfigJson) {
        this.containerConfigJson = containerConfigJson;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Long getCdJobId() {
        return cdJobId;
    }

    public void setCdJobId(Long cdJobId) {
        this.cdJobId = cdJobId;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Boolean getCheckEnvPermissionFlag() {
        return checkEnvPermissionFlag;
    }

    public void setCheckEnvPermissionFlag(Boolean checkEnvPermissionFlag) {
        this.checkEnvPermissionFlag = checkEnvPermissionFlag;
    }

    @Override
    public String toString() {
        return "DevopsCdEnvDeployInfoDTO{" +
                "id=" + id +
                ", appServiceId=" + appServiceId +
                ", envId=" + envId +
                ", valueId=" + valueId +
                ", projectId=" + projectId +
                ", deployType='" + deployType + '\'' +
                ", instanceId=" + instanceId +
                ", instanceName='" + instanceName + '\'' +
                ", cdJobId=" + cdJobId +
                ", jarName='" + jarName + '\'' +
                ", checkEnvPermissionFlag=" + checkEnvPermissionFlag +
                '}';
    }
}
