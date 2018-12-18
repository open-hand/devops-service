package io.choerodon.devops.api.dto;

import java.util.List;

public class DevopsEnvPreviewInstanceDTO extends ApplicationInstanceDTO {

    private List<ServiceDTO> serviceDTOS;
    private List<IngressDTO> ingressDTOS;
    private List<DaemonSetDTO> daemonSetDTOS;
    private List<StatefulSetDTO> statefulSetDTOS;
    private List<PersistentVolumeClaimDTO> persistentVolumeClaimDTOS;


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
