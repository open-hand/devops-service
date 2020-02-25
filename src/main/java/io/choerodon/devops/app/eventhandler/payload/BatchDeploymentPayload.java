package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;

/**
 * 批量部署的信息
 *
 * @author zmf
 * @since 2/24/20
 */
public class BatchDeploymentPayload {
    @ApiModelProperty("环境信息")
    private DevopsEnvironmentDTO devopsEnvironmentDTO;

    @ApiModelProperty("操作人的猪齿鱼用户id")
    private Long iamUserId;

    @ApiModelProperty("操作人的gitlab用户id")
    private Integer gitlabUserId;

    @ApiModelProperty("项目id")
    private Long projectId;

    @ApiModelProperty("实例相关信息，不可为空，不需要其中的service及ingress及环境信息")
    private List<InstanceSagaPayload> instanceSagaPayloads;

    @ApiModelProperty("网络相关信息，可为空, 其中元素不需要v1Endpoints及ingress及环境的信息")
    private List<ServiceSagaPayLoad> serviceSagaPayLoads;

    @ApiModelProperty("域名相关信息，可为空,其中元素不需要环境的信息")
    private List<IngressSagaPayload> ingressSagaPayloads;


    public DevopsEnvironmentDTO getDevopsEnvironmentDTO() {
        return devopsEnvironmentDTO;
    }

    public void setDevopsEnvironmentDTO(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        this.devopsEnvironmentDTO = devopsEnvironmentDTO;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public Integer getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Integer gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<InstanceSagaPayload> getInstanceSagaPayloads() {
        return instanceSagaPayloads;
    }

    public void setInstanceSagaPayloads(List<InstanceSagaPayload> instanceSagaPayloads) {
        this.instanceSagaPayloads = instanceSagaPayloads;
    }

    public List<ServiceSagaPayLoad> getServiceSagaPayLoads() {
        return serviceSagaPayLoads;
    }

    public void setServiceSagaPayLoads(List<ServiceSagaPayLoad> serviceSagaPayLoads) {
        this.serviceSagaPayLoads = serviceSagaPayLoads;
    }

    public List<IngressSagaPayload> getIngressSagaPayloads() {
        return ingressSagaPayloads;
    }

    public void setIngressSagaPayloads(List<IngressSagaPayload> ingressSagaPayloads) {
        this.ingressSagaPayloads = ingressSagaPayloads;
    }
}
