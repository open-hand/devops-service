package io.choerodon.devops.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by younger on 2018/4/25.
 */
public class DevopsEnvResourceDTO {
    private List<PodDTO> podDTOS;
    private List<ServiceDTO> serviceDTOS;
    private List<IngressDTO> ingressDTOS;
    private List<DeploymentDTO> deploymentDTOS;
    private List<ReplicaSetDTO> replicaSetDTOS;
    private List<DaemonSetDTO> daemonSetDTOS;
    private List<StatefulSetDTO> statefulSetDTOS;
    private List<PersistentVolumeClaimDTO> persistentVolumeClaimDTOS;

    /**
     * 构造函数
     */
    public DevopsEnvResourceDTO() {
        this.podDTOS = new ArrayList<>();
        this.deploymentDTOS = new ArrayList<>();
        this.serviceDTOS = new ArrayList<>();
        this.ingressDTOS = new ArrayList<>();
        this.replicaSetDTOS = new ArrayList<>();
        this.daemonSetDTOS = new ArrayList<>();
        this.statefulSetDTOS = new ArrayList<>();
        this.persistentVolumeClaimDTOS = new ArrayList<>();
    }

    public List<PodDTO> getPodDTOS() {
        return podDTOS;
    }

    public void setPodDTOS(List<PodDTO> podDTOS) {
        this.podDTOS = podDTOS;
    }

    public List<ServiceDTO> getServiceDTOS() {
        return serviceDTOS;
    }

    public void setServiceDTOS(List<ServiceDTO> serviceDTOS) {
        this.serviceDTOS = serviceDTOS;
    }

    public List<IngressDTO> getIngressDTOS() {
        return ingressDTOS;
    }

    public void setIngressDTOS(List<IngressDTO> ingressDTOS) {
        this.ingressDTOS = ingressDTOS;
    }

    public List<DeploymentDTO> getDeploymentDTOS() {
        return deploymentDTOS;
    }

    public void setDeploymentDTOS(List<DeploymentDTO> deploymentDTOS) {
        this.deploymentDTOS = deploymentDTOS;
    }

    public List<ReplicaSetDTO> getReplicaSetDTOS() {
        return replicaSetDTOS;
    }

    public void setReplicaSetDTOS(List<ReplicaSetDTO> replicaSetDTOS) {
        this.replicaSetDTOS = replicaSetDTOS;
    }

    public List<DaemonSetDTO> getDaemonSetDTOS() {
        return daemonSetDTOS;
    }

    public void setDaemonSetDTOS(List<DaemonSetDTO> daemonSetDTOS) {
        this.daemonSetDTOS = daemonSetDTOS;
    }

    public List<StatefulSetDTO> getStatefulSetDTOS() {
        return statefulSetDTOS;
    }

    public void setStatefulSetDTOS(List<StatefulSetDTO> statefulSetDTOS) {
        this.statefulSetDTOS = statefulSetDTOS;
    }

    public List<PersistentVolumeClaimDTO> getPersistentVolumeClaimDTOS() {
        return persistentVolumeClaimDTOS;
    }

    public void setPersistentVolumeClaimDTOS(List<PersistentVolumeClaimDTO> persistentVolumeClaimDTOS) {
        this.persistentVolumeClaimDTOS = persistentVolumeClaimDTOS;
    }
}
