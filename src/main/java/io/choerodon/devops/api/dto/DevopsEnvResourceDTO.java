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

    /**
     * 构造函数
     */
    public DevopsEnvResourceDTO() {
        this.podDTOS = new ArrayList<>();
        this.deploymentDTOS = new ArrayList<>();
        this.serviceDTOS = new ArrayList<>();
        this.ingressDTOS = new ArrayList<>();
        this.replicaSetDTOS = new ArrayList<>();
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

}
