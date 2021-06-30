package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 15:44
 */
public class DockerDeployDTO {
    @ApiModelProperty("镜像地址")
    private String image;
    @ApiModelProperty("容器名")
    private String name;
    @ApiModelProperty("主机端口")
    private Integer hostPort;
    @ApiModelProperty("容器端口")
    private Integer containerPort;
    @ApiModelProperty("镜像拉取账户信息")
    private DockerPullAccountDTO dockerPullAccountDTO;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public DockerPullAccountDTO getDockerPullAccountDTO() {
        return dockerPullAccountDTO;
    }

    public void setDockerPullAccountDTO(DockerPullAccountDTO dockerPullAccountDTO) {
        this.dockerPullAccountDTO = dockerPullAccountDTO;
    }
}
