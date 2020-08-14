package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/6 15:44
 */
public class CdPipelineEnvDeployVO {
    @Encrypt
    private Long appServiceId;
    @Encrypt
    private Long envId;
    @Encrypt
    private Long valueId;
    @Encrypt
    private Long projectId;
    private String deployType;
    @Encrypt
    private Long instanceId;
    private String instanceName;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
