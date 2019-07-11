package io.choerodon.devops.api.vo;

import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ApplicationVersionE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;

/**
 * Created by Sheep on 2019/7/4.
 */
public class InstanceSagaDTO {

    Long projectId;
    Long gitlabUserId;
    String secretCode;
    ApplicationE applicationE;
    ApplicationVersionE applicationVersionE;
    DevopsEnvironmentE devopsEnvironmentE;
    ApplicationDeployDTO applicationDeployDTO;


    public InstanceSagaDTO() {
    }


    public InstanceSagaDTO(Long projectId, Long gitlabUserId, String secretCode) {
        this.projectId = projectId;
        this.gitlabUserId = gitlabUserId;
        this.secretCode = secretCode;
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

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public ApplicationE getApplicationE() {
        return applicationE;
    }

    public void setApplicationE(ApplicationE applicationE) {
        this.applicationE = applicationE;
    }

    public ApplicationVersionE getApplicationVersionE() {
        return applicationVersionE;
    }

    public void setApplicationVersionE(ApplicationVersionE applicationVersionE) {
        this.applicationVersionE = applicationVersionE;
    }

    public DevopsEnvironmentE getDevopsEnvironmentE() {
        return devopsEnvironmentE;
    }

    public void setDevopsEnvironmentE(DevopsEnvironmentE devopsEnvironmentE) {
        this.devopsEnvironmentE = devopsEnvironmentE;
    }

    public ApplicationDeployDTO getApplicationDeployDTO() {
        return applicationDeployDTO;
    }

    public void setApplicationDeployDTO(ApplicationDeployDTO applicationDeployDTO) {
        this.applicationDeployDTO = applicationDeployDTO;
    }
}
