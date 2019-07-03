package io.choerodon.devops.api.dto;

import java.util.Date;

/**
 * Created by Sheep on 2019/7/2.
 */
public class DevopsCustomizeResourceDTO {

    private Long id;
    private Boolean envStatus;
    private String resourceContent;
    private String k8sKind;
    private String commandStatus;
    private String commandErrors;
    private String name;
    private String description;
    private Date lastUpdateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
    }

    public String getResourceContent() {
        return resourceContent;
    }

    public void setResourceContent(String resourceContent) {
        this.resourceContent = resourceContent;
    }

    public String getK8sKind() {
        return k8sKind;
    }

    public void setK8sKind(String k8sKind) {
        this.k8sKind = k8sKind;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getCommandErrors() {
        return commandErrors;
    }

    public void setCommandErrors(String commandErrors) {
        this.commandErrors = commandErrors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}
