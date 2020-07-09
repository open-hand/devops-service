package io.choerodon.devops.api.vo;

import java.util.List;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:35 2019/4/4
 * Description:
 */
public class PipelineAppServiceDeployVO {
    @Encrypt
    private Long id;
    @Encrypt
    private Long appServiceId;
    private List<String> triggerVersion;
    @Encrypt
    private Long envId;
    @Encrypt
    private Long instanceId;
    @Encrypt
    private Long stageId;
    private String instanceName;
    private String value;
    @Encrypt
    private Long valueId;
    @Encrypt
    private Long projectId;
    private String appServiceName;
    private String envName;
    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public List<String> getTriggerVersion() {
        return triggerVersion;
    }

    public void setTriggerVersion(List<String> triggerVersion) {
        this.triggerVersion = triggerVersion;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
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

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

}
