package io.choerodon.devops.api.vo;

import java.util.List;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.constant.EncryptKeyConstants;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:35 2019/4/4
 * Description:
 */
public class PipelineAppServiceDeployVO {
    private Long id;
    @Encrypt(EncryptKeyConstants.DEVOPS_APP_SERVICE_ENCRYPT_KEY)
    private Long appServiceId;
    private List<String> triggerVersion;
    @Encrypt(EncryptKeyConstants.DEVOPS_ENV_ENCRYPT_KEY)
    private Long envId;
    @Encrypt(EncryptKeyConstants.DDEVOPS_APP_SERVICE_INSTANCE_ENCRYPT_KEY)
    private Long instanceId;
    @Encrypt(EncryptKeyConstants.DEVOPS_PIPELINE_STAGE_ENCRYPT_KEY)
    private Long stageId;
    private String instanceName;
    private String value;
    @Encrypt(EncryptKeyConstants.DEVOPS_DEPLOY_VALUE_ENCRYPT_KEY)
    private Long valueId;
    @Encrypt(EncryptKeyConstants.IAM_PROJECT_ENCRYPT_KEY)
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
