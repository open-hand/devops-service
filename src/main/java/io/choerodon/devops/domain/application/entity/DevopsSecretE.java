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
    private Map<String, String> value;
    private Long commandId;
    private Long ObjectVersionNumber;
    private Date lastUpdateDate;
    private String status;
    private String commandType;

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

    public Map<String, String> getValue() {
        return value;
    }

    public void setValue(Map<String, String> value) {
        this.value = value;
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

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}
