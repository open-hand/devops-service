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
    @ApiModelProperty("实例id")
    private String instanceId;
    @ApiModelProperty("镜像地址")
    private String image;
    @ApiModelProperty("容器名")
    private String name;
    @ApiModelProperty("容器id")
    private String containerId;
    @ApiModelProperty("镜像拉取账户信息")
    private DockerPullAccountDTO dockerPullAccountDTO;

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    private String cmd;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

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

    public DockerPullAccountDTO getDockerPullAccountDTO() {
        return dockerPullAccountDTO;
    }

    public void setDockerPullAccountDTO(DockerPullAccountDTO dockerPullAccountDTO) {
        this.dockerPullAccountDTO = dockerPullAccountDTO;
    }
}
