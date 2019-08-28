package io.choerodon.devops.app.eventhandler.payload;

import io.kubernetes.client.models.V1beta1Ingress;

import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsIngressDTO;

/**
 * Created by Sheep on 2019/7/29.
 */
public class IngressSagaPayload {

    private Long projectId;
    private Long gitlabUserId;
    private Boolean created;
    private DevopsEnvironmentDTO devopsEnvironmentDTO;
    private DevopsIngressDTO devopsIngressDTO;
    private V1beta1Ingress v1beta1Ingress;


    public IngressSagaPayload() {
    }

    public IngressSagaPayload(Long projectId, Long gitlabUserId) {
        this.projectId = projectId;
        this.gitlabUserId = gitlabUserId;
    }


    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }

    public DevopsEnvironmentDTO getDevopsEnvironmentDTO() {
        return devopsEnvironmentDTO;
    }

    public void setDevopsEnvironmentDTO(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        this.devopsEnvironmentDTO = devopsEnvironmentDTO;
    }

    public DevopsIngressDTO getDevopsIngressDTO() {
        return devopsIngressDTO;
    }

    public void setDevopsIngressDTO(DevopsIngressDTO devopsIngressDTO) {
        this.devopsIngressDTO = devopsIngressDTO;
    }

    public V1beta1Ingress getV1beta1Ingress() {
        return v1beta1Ingress;
    }

    public void setV1beta1Ingress(V1beta1Ingress v1beta1Ingress) {
        this.v1beta1Ingress = v1beta1Ingress;
    }

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }
}
