package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 环境的资源的统计数据
 *
 * @author zmf
 */
public class DevopsEnvResourceCountVO {
    @Encrypt
    private Long envId;

    private Long instanceCount;

    private Long serviceCount;

    private Long ingressCount;

    private Long certificationCount;

    private Long configMapCount;

    private Long secretCount;

    private Long customCount;

    private Long runningInstanceCount;

    private Long operatingInstanceCount;

    private Long stoppedInstanceCount;

    private Long failedInstanceCount;

    private Long pvcCount;

    private Long workloadCount;

    private Long deploymentCount;

    private Long jobCount;

    private Long daemonSetCount;

    private Long statefulSetCount;

    private Long cronJobCount;

    public Long getDeploymentCount() {
        return deploymentCount;
    }

    public void setDeploymentCount(Long deploymentCount) {
        this.deploymentCount = deploymentCount;
    }

    public Long getJobCount() {
        return jobCount;
    }

    public void setJobCount(Long jobCount) {
        this.jobCount = jobCount;
    }

    public Long getDaemonSetCount() {
        return daemonSetCount;
    }

    public void setDaemonSetCount(Long daemonSetCount) {
        this.daemonSetCount = daemonSetCount;
    }

    public Long getStatefulSetCount() {
        return statefulSetCount;
    }

    public void setStatefulSetCount(Long statefulSetCount) {
        this.statefulSetCount = statefulSetCount;
    }

    public Long getCronJobCount() {
        return cronJobCount;
    }

    public void setCronJobCount(Long cronJobCount) {
        this.cronJobCount = cronJobCount;
    }

    private Long podCount;

    public Long getWorkloadCount() {
        return workloadCount;
    }

    public void setWorkloadCount(Long workloadCount) {
        this.workloadCount = workloadCount;
    }

    public Long getPodCount() {
        return podCount;
    }

    public void setPodCount(Long podCount) {
        this.podCount = podCount;
    }

    public Long getPvcCount() {
        return pvcCount;
    }

    public void setPvcCount(Long pvcCount) {
        this.pvcCount = pvcCount;
    }

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

    public Long getCustomCount() {
        return customCount;
    }

    public void setCustomCount(Long customCount) {
        this.customCount = customCount;
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
