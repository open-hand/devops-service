package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 环境的资源的统计数据
 *
 * @author zmf
 */
public class DevopsEnvResourceCountVO {
    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;
    @ApiModelProperty("实例总数")
    private Long instanceCount;
    @ApiModelProperty("service总数")
    private Long serviceCount;
    @ApiModelProperty("ingress总数")
    private Long ingressCount;
    @ApiModelProperty("证书总数")
    private Long certificationCount;
    @ApiModelProperty("configmap总数")
    private Long configMapCount;
    @ApiModelProperty("secret总数")
    private Long secretCount;
    @ApiModelProperty("自定义资源总数")
    private Long customCount;
    @ApiModelProperty("运行实例总数")
    private Long runningInstanceCount;
    @ApiModelProperty("操作中实例总数")
    private Long operatingInstanceCount;
    @ApiModelProperty("停用实例总数")
    private Long stoppedInstanceCount;
    @ApiModelProperty("失败实例总数")
    private Long failedInstanceCount;
    @ApiModelProperty("pvc总数")
    private Long pvcCount;
    @ApiModelProperty("工作负载总数")
    private Long workloadCount;
    @ApiModelProperty("deployment总数")
    private Long deploymentCount;
    @ApiModelProperty("job总数")
    private Long jobCount;
    @ApiModelProperty("daemonset总数")
    private Long daemonSetCount;
    @ApiModelProperty("statefulset总数")
    private Long statefulSetCount;
    @ApiModelProperty("cronjob总数")
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
