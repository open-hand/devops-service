package io.choerodon.devops.api.dto;

import java.util.List;

public class DevopsEnvPreviewInstanceDTO extends ApplicationInstanceDTO {

    private List<ServiceDTO> serviceDTOS;
    private List<IngressDTO> ingressDTOS;


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
}
