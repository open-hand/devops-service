package io.choerodon.devops.api.vo.iam.entity;

import java.util.Date;
import java.util.List;

public class DevopsConfigMapE {

    private Long id;
    private DevopsEnvironmentE devopsEnvironmentE;
    private DevopsEnvCommandE devopsEnvCommandE;
    private String name;
    private String description;
    private String value;
    private String envCode;
    private List<String> key;
    private Boolean envStatus;
    private String commandType;
    private String commandStatus;
    private Date lastUpdateDate;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DevopsEnvironmentE getDevopsEnvironmentE() {
        return devopsEnvironmentE;
    }

    public void initDevopsEnvironmentE(Long id) {
        this.devopsEnvironmentE = new DevopsEnvironmentE(id);
    }

    public DevopsEnvCommandE getDevopsEnvCommandE() {
        return devopsEnvCommandE;
    }

    public void initDevopsEnvCommandE(Long id) {
        this.devopsEnvCommandE = new DevopsEnvCommandE(id);
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }


    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
    }

    public List<String> getKey() {
        return key;
    }

    public void setKey(List<String> key) {
        this.key = key;
    }
}
