package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.kubernetes.client.models.V1PersistentVolume;

public class PersistentVolumePayload {
    private Long projectId;
    private Long gitlabUserId;
    private Boolean created;
    private DevopsEnvironmentDTO devopsEnvironmentDTO;
    private DevopsPvDTO devopsPvDTO;

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

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    public DevopsEnvironmentDTO getDevopsEnvironmentDTO() {
        return devopsEnvironmentDTO;
    }

    public void setDevopsEnvironmentDTO(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        this.devopsEnvironmentDTO = devopsEnvironmentDTO;
    }

    public DevopsPvDTO getDevopsPvDTO() {
        return devopsPvDTO;
    }

    public void setDevopsPvDTO(DevopsPvDTO devopsPvDTO) {
        this.devopsPvDTO = devopsPvDTO;
    }

    public V1PersistentVolume getV1PersistentVolume() {
        return v1PersistentVolume;
    }

    public void setV1PersistentVolume(V1PersistentVolume v1PersistentVolume) {
        this.v1PersistentVolume = v1PersistentVolume;
    }

    private V1PersistentVolume v1PersistentVolume;


    public PersistentVolumePayload() {
    }

    public PersistentVolumePayload(Long projectId, Long gitlabUserId) {
        this.projectId = projectId;
        this.gitlabUserId = gitlabUserId;
    }
}