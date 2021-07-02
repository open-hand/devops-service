package io.choerodon.devops.api.vo.host;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/2 10:52
 */

public class DevopsDockerInstanceVO extends AuditDomain {

    private Long id;

    @ApiModelProperty("主机id")
    private Long hostId;
    @ApiModelProperty("容器名")
    private String name;
    @ApiModelProperty("容器id")
    private String containerId;
    @ApiModelProperty("镜像名")
    private String image;
    @ApiModelProperty("主机端口")
    private Integer hostPort;
    @ApiModelProperty("容器端口")
    private Integer containerPort;
    /**
     * {@link io.choerodon.devops.infra.enums.deploy.DockerInstanceStatusEnum}
     */
    @ApiModelProperty("容器状态")
    private String status;
    @ApiModelProperty("部署来源")
    private String sourceType;

    private IamUserDTO deployer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public IamUserDTO getDeployer() {
        return deployer;
    }

    public void setDeployer(IamUserDTO deployer) {
        this.deployer = deployer;
    }
}
