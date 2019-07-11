package io.choerodon.devops.domain.application.valueobject;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.DevopsIngressDTO;
import io.choerodon.devops.api.vo.EndPointPortDTO;
import io.choerodon.devops.domain.application.entity.PortMapE;
import io.choerodon.devops.infra.dataobject.ServiceInstanceDO;

/**
 * Created by Zenger on 2018/4/19.
 */
public class DevopsServiceV {

    private Long id;
    private String name;
    private String externalIp;
    private String loadBalanceIp;
    private List<PortMapE> ports;
    private String status;
    private Long envId;
    private String type;
    private String envName;
    private Boolean envStatus;
    private String namespace;
    private Long appId;
    private Long appProjectId;
    private String appName;
    private Map<String, List<EndPointPortDTO>> endPoinits;
    private Map<String, String> labels;
    private List<ServiceInstanceDO> appInstance;
    private String commandType;
    private String commandStatus;
    private String error;
    private List<DevopsIngressDTO> devopsIngressDTOS;

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

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<ServiceInstanceDO> getAppInstance() {
        return appInstance;
    }

    public void setAppInstance(List<ServiceInstanceDO> appInstance) {
        this.appInstance = appInstance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getEnvStatus() {
        return envStatus;
    }

    public void setEnvStatus(Boolean envStatus) {
        this.envStatus = envStatus;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<PortMapE> getPorts() {
        return ports;
    }

    public void setPorts(List<PortMapE> ports) {
        this.ports = ports;
    }

    public Long getAppProjectId() {
        return appProjectId;
    }

    public void setAppProjectId(Long appProjectId) {
        this.appProjectId = appProjectId;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, List<EndPointPortDTO>> getEndPoinits() {
        return endPoinits;
    }

    public void setEndPoinits(Map<String, List<EndPointPortDTO>> endPoinits) {
        this.endPoinits = endPoinits;
    }

    public String getLoadBalanceIp() {
        return loadBalanceIp;
    }

    public void setLoadBalanceIp(String loadBalanceIp) {
        this.loadBalanceIp = loadBalanceIp;
    }


    public List<DevopsIngressDTO> getDevopsIngressDTOS() {
        return devopsIngressDTOS;
    }

    public void setDevopsIngressDTOS(List<DevopsIngressDTO> devopsIngressDTOS) {
        this.devopsIngressDTOS = devopsIngressDTOS;
    }
}
