package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by younger on 2018/4/25.
 */
public class DevopsEnvResourceVO {
    private List<PodVO> podVOS;
    private List<ServiceVO> serviceVOS;
    private List<IngressVO> ingressVOS;
    private List<DeploymentVO> deploymentVOS;
    private List<ReplicaSetVO> replicaSetVOS;
    private List<DaemonSetVO> daemonSetVOS;
    private List<StatefulSetVO> statefulSetVOS;
    private List<PersistentVolumeClaimVO> persistentVolumeClaimVOS;

    /**
     * 构造函数
     */
    public DevopsEnvResourceVO() {
        this.podVOS = new ArrayList<>();
        this.deploymentVOS = new ArrayList<>();
        this.serviceVOS = new ArrayList<>();
        this.ingressVOS = new ArrayList<>();
        this.replicaSetVOS = new ArrayList<>();
        this.daemonSetVOS = new ArrayList<>();
        this.statefulSetVOS = new ArrayList<>();
        this.persistentVolumeClaimVOS = new ArrayList<>();
    }

    public List<PodVO> getPodVOS() {
        return podVOS;
    }

    public void setPodVOS(List<PodVO> podVOS) {
        this.podVOS = podVOS;
    }

    public List<ServiceVO> getServiceVOS() {
        return serviceVOS;
    }

    public void setServiceVOS(List<ServiceVO> serviceVOS) {
        this.serviceVOS = serviceVOS;
    }

    public List<IngressVO> getIngressVOS() {
        return ingressVOS;
    }

    public void setIngressVOS(List<IngressVO> ingressVOS) {
        this.ingressVOS = ingressVOS;
    }

    public List<DeploymentVO> getDeploymentVOS() {
        return deploymentVOS;
    }

    public void setDeploymentVOS(List<DeploymentVO> deploymentVOS) {
        this.deploymentVOS = deploymentVOS;
    }

    public List<ReplicaSetVO> getReplicaSetVOS() {
        return replicaSetVOS;
    }

    public void setReplicaSetVOS(List<ReplicaSetVO> replicaSetVOS) {
        this.replicaSetVOS = replicaSetVOS;
    }

    public List<DaemonSetVO> getDaemonSetVOS() {
        return daemonSetVOS;
    }

    public void setDaemonSetVOS(List<DaemonSetVO> daemonSetVOS) {
        this.daemonSetVOS = daemonSetVOS;
    }

    public List<StatefulSetVO> getStatefulSetVOS() {
        return statefulSetVOS;
    }

    public void setStatefulSetVOS(List<StatefulSetVO> statefulSetVOS) {
        this.statefulSetVOS = statefulSetVOS;
    }

    public List<PersistentVolumeClaimVO> getPersistentVolumeClaimVOS() {
        return persistentVolumeClaimVOS;
    }

    public void setPersistentVolumeClaimVOS(List<PersistentVolumeClaimVO> persistentVolumeClaimVOS) {
        this.persistentVolumeClaimVOS = persistentVolumeClaimVOS;
    }
}
