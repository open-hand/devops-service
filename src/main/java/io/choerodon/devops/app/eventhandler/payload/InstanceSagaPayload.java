package io.choerodon.devops.app.eventhandler.payload;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;

/**
 * Created by Sheep on 2019/7/4.
 */
public class InstanceSagaPayload {

    private Long projectId;
    private Long gitlabUserId;
    private Long commandId;
    private String secretCode;
    private AppServiceDTO applicationDTO;
    private AppServiceVersionDTO appServiceVersionDTO;
    private DevopsEnvironmentDTO devopsEnvironmentDTO;
    private AppServiceDeployVO appServiceDeployVO;
    private DevopsServiceReqVO devopsServiceReqVO;
    private DevopsIngressVO devopsIngressVO;

    @ApiModelProperty("replicas生效策略在 values/deployment")
    private String replicasStrategy;

    public String getReplicasStrategy() {
        return replicasStrategy;
    }

    public void setReplicasStrategy(String replicasStrategy) {
        this.replicasStrategy = replicasStrategy;
    }


    public InstanceSagaPayload() {
    }


    public InstanceSagaPayload(Long projectId, Long gitlabUserId, String secretCode, Long commandId) {
        this.projectId = projectId;
        this.gitlabUserId = gitlabUserId;
        this.secretCode = secretCode;
        this.commandId = commandId;
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

    public AppServiceDTO getApplicationDTO() {
        return applicationDTO;
    }

    public void setApplicationDTO(AppServiceDTO applicationDTO) {
        this.applicationDTO = applicationDTO;
    }

    public AppServiceVersionDTO getAppServiceVersionDTO() {
        return appServiceVersionDTO;
    }

    public void setAppServiceVersionDTO(AppServiceVersionDTO appServiceVersionDTO) {
        this.appServiceVersionDTO = appServiceVersionDTO;
    }

    public DevopsEnvironmentDTO getDevopsEnvironmentDTO() {
        return devopsEnvironmentDTO;
    }

    public void setDevopsEnvironmentDTO(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        this.devopsEnvironmentDTO = devopsEnvironmentDTO;
    }

    public AppServiceDeployVO getAppServiceDeployVO() {
        return appServiceDeployVO;
    }

    public void setAppServiceDeployVO(AppServiceDeployVO appServiceDeployVO) {
        this.appServiceDeployVO = appServiceDeployVO;
    }

    public DevopsServiceReqVO getDevopsServiceReqVO() {
        return devopsServiceReqVO;
    }

    public void setDevopsServiceReqVO(DevopsServiceReqVO devopsServiceReqVO) {
        this.devopsServiceReqVO = devopsServiceReqVO;
    }

    public DevopsIngressVO getDevopsIngressVO() {
        return devopsIngressVO;
    }

    public void setDevopsIngressVO(DevopsIngressVO devopsIngressVO) {
        this.devopsIngressVO = devopsIngressVO;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
}
