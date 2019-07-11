package io.choerodon.devops.api.vo.iam.entity;

import java.util.List;

/**
 * Created by Zenger on 2018/4/18.
 */
public class DevopsServiceE {

    private Long id;
    private Long envId;
    private Long appId;
    private String name;
    private String status;
    private List<PortMapE> ports;
    private String endPoints;
    private String externalIp;
    private String type;
    private String labels;
    private String annotations;
    private Long commandId;
    private String loadBalanceIp;
    private Long objectVersionNumber;

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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public List<PortMapE> getPorts() {
        return ports;
    }

    public void setPorts(List<PortMapE> ports) {
        this.ports = ports;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(String endPoints) {
        this.endPoints = endPoints;
    }

    public String getLoadBalanceIp() {
        return loadBalanceIp;
    }

    public void setLoadBalanceIp(String loadBalanceIp) {
        this.loadBalanceIp = loadBalanceIp;
    }
}
