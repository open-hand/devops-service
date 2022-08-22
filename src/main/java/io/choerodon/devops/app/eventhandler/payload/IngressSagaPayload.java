package io.choerodon.devops.app.eventhandler.payload;

import io.kubernetes.client.common.KubernetesObject;

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
    private String ingressJson;
    private Boolean operateForOldIngress;


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

    public String getIngressJson() {
        return ingressJson;
    }

    public void setIngressJson(String ingressJson) {
        this.ingressJson = ingressJson;
    }

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    public Boolean getOperateForOldIngress() {
        return operateForOldIngress;
    }

    public void setOperateForOldIngress(Boolean operateForOldIngress) {
        this.operateForOldIngress = operateForOldIngress;
    }
}
