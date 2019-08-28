package io.choerodon.devops.api.vo;

import java.util.UUID;

/**
 * @author zmf
 */
public class ContainerVO {
    private String name;
    private Boolean isReady;
    private String registry;
    private String logId;

    public ContainerVO() {
        this.logId = UUID.randomUUID().toString();
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }
}
