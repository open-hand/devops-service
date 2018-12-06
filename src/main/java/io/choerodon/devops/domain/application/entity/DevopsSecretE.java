package io.choerodon.devops.domain.application.entity;

import java.util.Date;
import java.util.Map;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:15
 * Description:
 */
public class DevopsSecretE {

    private Long id;
    private Long envId;
    private String name;
    private String description;
    private Map<String, String> secretMaps;
    private Long commandId;
    private Long ObjectVersionNumber;
    private Date creationDate;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
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

    public Map<String, String> getSecretMaps() {
        return secretMaps;
    }

    public void setSecretMaps(Map<String, String> secretMaps) {
        this.secretMaps = secretMaps;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Long getObjectVersionNumber() {
        return ObjectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        ObjectVersionNumber = objectVersionNumber;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
