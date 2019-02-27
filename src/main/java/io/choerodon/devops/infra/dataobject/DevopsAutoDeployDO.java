package io.choerodon.devops.infra.dataobject;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:12 2019/2/25
 * Description:
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_auto_deploy")
public class DevopsAutoDeployDO {

    @Id
    @GeneratedValue
    private Long id;
    private String taskName;
    private Long appId;
    private String triggerVersion;
    private Long envId;
    private Long valueId;
    private Long projectId;
    private Long objectVersionNumber;
    private Date lastUpdateDate;

    @Transient
    private String appName;
    @Transient
    private String envName;
    @Transient
    private Integer envStatus;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getTriggerVersion() {
        return triggerVersion;
    }

    public void setTriggerVersion(String triggerVersion) {
        this.triggerVersion = triggerVersion;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Integer getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Integer envStatus) {
        this.envStatus = envStatus;
    }
}

