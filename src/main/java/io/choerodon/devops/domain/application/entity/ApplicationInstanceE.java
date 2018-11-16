package io.choerodon.devops.domain.application.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/4/12.
 */
@Component
@Scope("prototype")
public class ApplicationInstanceE {

    private Long id;
    private String code;
    private String publishLevel;
    private String contributor;
    private String description;
    private ApplicationE applicationE;
    private ApplicationVersionE applicationVersionE;
    private DevopsEnvironmentE devopsEnvironmentE;
    private Long objectVersionNumber;
    private String status;
    private Long commandId;
    private Long podCount;
    private Long podRunningCount;
    private String commandStatus;
    private String commandType;
    private String commandVersion;
    private Long commandVersionId;
    private String error;
    private Boolean isConnect;
    private Long projectId;

    public ApplicationInstanceE() {
    }

    public ApplicationInstanceE(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ApplicationVersionE getApplicationVersionE() {
        return applicationVersionE;
    }

    public void setApplicationVersionE(ApplicationVersionE applicationVersionE) {
        this.applicationVersionE = applicationVersionE;
    }

    public ApplicationE getApplicationE() {
        return applicationE;
    }

    public void setApplicationE(ApplicationE applicationE) {
        this.applicationE = applicationE;
    }

    public DevopsEnvironmentE getDevopsEnvironmentE() {
        return devopsEnvironmentE;
    }

    public void setDevopsEnvironmentE(DevopsEnvironmentE devopsEnvironmentE) {
        this.devopsEnvironmentE = devopsEnvironmentE;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getPodCount() {
        return podCount;
    }

    public void setPodCount(Long podCount) {
        this.podCount = podCount;
    }

    public Long getPodRunningCount() {
        return podRunningCount;
    }

    public void setPodRunningCount(Long podRunningCount) {
        this.podRunningCount = podRunningCount;
    }

    public void initApplicationVersionE(Long id, String version) {
        this.applicationVersionE = new ApplicationVersionE(id, version);
    }

    public void initDevopsEnvironmentE(Long id, String code, String name) {
        this.devopsEnvironmentE = new DevopsEnvironmentE(id, code, name);
    }

    public void initDevopsEnvironmentEById(Long id) {
        this.devopsEnvironmentE = new DevopsEnvironmentE(id);
    }

    public void initApplicationVersionEById(Long id) {
        this.applicationVersionE = new ApplicationVersionE(id);
    }

    public void initApplicationE(Long appId, String name) {
        this.applicationE = new ApplicationE(appId, name);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void initApplicationEById(Long appId) {
        this.applicationE = new ApplicationE(appId);
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPublishLevel() {
        return publishLevel;
    }

    public void setPublishLevel(String publishLevel) {
        this.publishLevel = publishLevel;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getConnect() {
        return isConnect;
    }

    public void setConnect(Boolean connect) {
        isConnect = connect;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getCommandVersion() {
        return commandVersion;
    }

    public void setCommandVersion(String commandVersion) {
        this.commandVersion = commandVersion;
    }

    public Long getCommandVersionId() {
        return commandVersionId;
    }

    public void setCommandVersionId(Long commandVersionId) {
        this.commandVersionId = commandVersionId;
    }
}
