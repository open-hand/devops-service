package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 11:53
 */
@Table(name = "devops_docker_instance")
@ModifyAudit
@VersionAudit
public class DevopsDockerInstanceDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("主机id")
    private Long hostId;
    @ApiModelProperty("容器名")
    private String name;
    @ApiModelProperty("容器id")
    private String containerId;
    @ApiModelProperty("镜像名")
    private String image;
    @ApiModelProperty("端口映射列表")
    private String ports;
    /**
     * {@link io.choerodon.devops.infra.enums.deploy.DockerInstanceStatusEnum}
     */
    @ApiModelProperty("容器状态")
    private String status;
    /**
     * {@link io.choerodon.devops.infra.enums.AppSourceType}
     */
    @ApiModelProperty("部署来源")
    private String sourceType;

    public DevopsDockerInstanceDTO() {
    }

    public DevopsDockerInstanceDTO(Long hostId, String name) {
        this.hostId = hostId;
        this.name = name;
    }

    public DevopsDockerInstanceDTO(Long hostId, String name, String image, String status, String sourceType) {
        this.hostId = hostId;
        this.name = name;
        this.image = image;
        this.status = status;
        this.sourceType = sourceType;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

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
}
