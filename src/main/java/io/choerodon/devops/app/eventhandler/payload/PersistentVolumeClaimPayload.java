package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsPvcDTO;
import io.kubernetes.client.models.V1PersistentVolumeClaim;

public class PersistentVolumeClaimPayload {
    private Long projectId;
    private Long gitlabUserId;
    private Boolean created;
    private DevopsEnvironmentDTO devopsEnvironmentDTO;
    private DevopsPvcDTO devopsPvcDTO;
    private V1PersistentVolumeClaim v1PersistentVolumeClaim;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PersistentVolumeClaimPayload() {
    }

    public PersistentVolumeClaimPayload(Long projectId, Long gitlabUserId) {
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

    public DevopsPvcDTO getDevopsPvcDTO() {
        return devopsPvcDTO;
    }

    public void setDevopsPvcDTO(DevopsPvcDTO devopsPvcDTO) {
        this.devopsPvcDTO = devopsPvcDTO;
    }

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    public V1PersistentVolumeClaim getV1PersistentVolumeClaim() {
        return v1PersistentVolumeClaim;
    }

    public void setV1PersistentVolumeClaim(V1PersistentVolumeClaim v1PersistentVolumeClaim) {
        this.v1PersistentVolumeClaim = v1PersistentVolumeClaim;
    }
}
