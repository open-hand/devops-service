package io.choerodon.devops.api.vo;

/**
 * 环境的资源的统计数据
 * @author zmf
 */
public class DevopsEnvResourceCountVO {
    private Long envId;

    private Long instanceCount;

    private Long serviceCount;

    private Long ingressCount;

    private Long certificationCount;

    private Long configMapCount;

    private Long secretCount;

    private Long runningInstanceCount;

    private Long operatingInstanceCount;

    private Long stoppedInstanceCount;

    private Long failedInstanceCount;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Long instanceCount) {
        this.instanceCount = instanceCount;
    }

    public Long getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(Long serviceCount) {
        this.serviceCount = serviceCount;
    }

    public Long getIngressCount() {
        return ingressCount;
    }

    public void setIngressCount(Long ingressCount) {
        this.ingressCount = ingressCount;
    }

    public Long getCertificationCount() {
        return certificationCount;
    }

    public void setCertificationCount(Long certificationCount) {
        this.certificationCount = certificationCount;
    }

    public Long getConfigMapCount() {
        return configMapCount;
    }

    public void setConfigMapCount(Long configMapCount) {
        this.configMapCount = configMapCount;
    }

    public Long getSecretCount() {
        return secretCount;
    }

    public void setSecretCount(Long secretCount) {
        this.secretCount = secretCount;
    }

    public Long getRunningInstanceCount() {
        return runningInstanceCount;
    }

    public void setRunningInstanceCount(Long runningInstanceCount) {
        this.runningInstanceCount = runningInstanceCount;
    }

    public Long getOperatingInstanceCount() {
        return operatingInstanceCount;
    }

    public void setOperatingInstanceCount(Long operatingInstanceCount) {
        this.operatingInstanceCount = operatingInstanceCount;
    }

    public Long getStoppedInstanceCount() {
        return stoppedInstanceCount;
    }

    public void setStoppedInstanceCount(Long stoppedInstanceCount) {
        this.stoppedInstanceCount = stoppedInstanceCount;
    }

    public Long getFailedInstanceCount() {
        return failedInstanceCount;
    }

    public void setFailedInstanceCount(Long failedInstanceCount) {
        this.failedInstanceCount = failedInstanceCount;
    }
}
