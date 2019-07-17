package io.choerodon.devops.api.vo;

import java.util.List;

public class DevopsEnvPreviewInstanceVO extends ApplicationInstanceVO {

    private List<ServiceVO> serviceVOS;
    private List<IngressVO> ingressVOS;
    private List<DaemonSetVO> daemonSetVOS;
    private List<StatefulSetVO> statefulSetVOS;
    private List<PersistentVolumeClaimVO> persistentVolumeClaimVOS;


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
