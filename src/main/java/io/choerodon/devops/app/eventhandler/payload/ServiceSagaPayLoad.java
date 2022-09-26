package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.kubernetes.client.openapi.models.V1Endpoints;
import io.kubernetes.client.openapi.models.V1Service;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Sheep on 2019/7/29.
 */
public class ServiceSagaPayLoad {

    private Long projectId;
    private Long gitlabUserId;
    @ApiModelProperty("是否创建网络/true表示这个是创建操作")
    private Boolean created;
    private DevopsEnvironmentDTO devopsEnvironmentDTO;
    private DevopsServiceDTO devopsServiceDTO;
    private V1Service v1Service;
    private V1Endpoints v1Endpoints;
    private DevopsIngressVO devopsIngressVO;

    public ServiceSagaPayLoad() {
    }

    public ServiceSagaPayLoad(Long projectId, Long gitlabUserId) {
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

    public DevopsServiceDTO getDevopsServiceDTO() {
        return devopsServiceDTO;
    }

    public void setDevopsServiceDTO(DevopsServiceDTO devopsServiceDTO) {
        this.devopsServiceDTO = devopsServiceDTO;
    }

    public V1Service getV1Service() {
        return v1Service;
    }

    public void setV1Service(V1Service v1Service) {
        this.v1Service = v1Service;
    }

    public V1Endpoints getV1Endpoints() {
        return v1Endpoints;
    }

    public void setV1Endpoints(V1Endpoints v1Endpoints) {
        this.v1Endpoints = v1Endpoints;
    }

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    public DevopsIngressVO getDevopsIngressVO() {
        return devopsIngressVO;
    }

    public void setDevopsIngressVO(DevopsIngressVO devopsIngressVO) {
        this.devopsIngressVO = devopsIngressVO;
    }
}
