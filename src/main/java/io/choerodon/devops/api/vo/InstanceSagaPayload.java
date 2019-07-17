package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.ApplicationDTO;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;

/**
 * Created by Sheep on 2019/7/4.
 */
public class InstanceSagaPayload {

    private Long projectId;
    private Long gitlabUserId;
    private String secretCode;
    private ApplicationDTO applicationDTO;
    private ApplicationVersionDTO applicationVersionDTO;
    private DevopsEnvironmentDTO devopsEnvironmentDTO;
    private ApplicationDeployVO applicationDeployVO;


    public InstanceSagaPayload() {
    }


    public InstanceSagaPayload(Long projectId, Long gitlabUserId, String secretCode) {
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

    public ApplicationDTO getApplicationDTO() {
        return applicationDTO;
    }

    public void setApplicationDTO(ApplicationDTO applicationDTO) {
        this.applicationDTO = applicationDTO;
    }

    public ApplicationVersionDTO getApplicationVersionDTO() {
        return applicationVersionDTO;
    }

    public void setApplicationVersionDTO(ApplicationVersionDTO applicationVersionDTO) {
        this.applicationVersionDTO = applicationVersionDTO;
    }

    public DevopsEnvironmentDTO getDevopsEnvironmentDTO() {
        return devopsEnvironmentDTO;
    }

    public void setDevopsEnvironmentDTO(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        this.devopsEnvironmentDTO = devopsEnvironmentDTO;
    }

    public ApplicationDeployVO getApplicationDeployVO() {
        return applicationDeployVO;
    }

    public void setApplicationDeployVO(ApplicationDeployVO applicationDeployVO) {
        this.applicationDeployVO = applicationDeployVO;
    }
}
