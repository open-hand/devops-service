package io.choerodon.devops.api.vo;

import java.util.Date;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.annotation.WillDeleted;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:52 2019/4/4
 * Description:
 */
@WillDeleted
public class PipelineVO {
    @Encrypt
    private Long id;
    private String name;
    private String triggerType;
    private Integer isEnabled;
    @Encrypt
    private Long projectId;
    private Long objectVersionNumber;
    private Date lastUpdateDate;
    private String createUserUrl;
    private String createUserName;
    private String createUserRealName;
    @Encrypt
    private Long createdBy;
    private Boolean isExecute;
    private Boolean edit;
    private String envName;

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public Boolean getExecute() {
        return isExecute;
    }

    public void setExecute(Boolean execute) {
        isExecute = execute;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public String getCreateUserUrl() {
        return createUserUrl;
    }

    public void setCreateUserUrl(String createUserUrl) {
        this.createUserUrl = createUserUrl;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getCreateUserRealName() {
        return createUserRealName;
    }

    public void setCreateUserRealName(String createUserRealName) {
        this.createUserRealName = createUserRealName;
    }
}
