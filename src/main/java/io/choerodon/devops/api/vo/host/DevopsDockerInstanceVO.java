package io.choerodon.devops.api.vo.host;

import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/2 10:52
 */

public class DevopsDockerInstanceVO extends DevopsHostInstanceVO {

    @ApiModelProperty("容器id")
    private String containerId;
    @ApiModelProperty("镜像名")
    private String image;
    @ApiModelProperty("端口映射列表")
    private String ports;
    /**
     * {@link io.choerodon.devops.infra.enums.deploy.DockerInstanceStatusEnum}
     */

    @ApiModelProperty("部署来源")
    private String sourceType;


    private List<DockerPortMapping> portMappingList;

    private IamUserDTO deployer;

    public IamUserDTO getDeployer() {
        return deployer;
    }

    public void setDeployer(IamUserDTO deployer) {
        this.deployer = deployer;
    }

    @ApiModelProperty("操作命令")
    private DevopsHostCommandDTO devopsHostCommandDTO;

    public DevopsHostCommandDTO getDevopsHostCommandDTO() {
        return devopsHostCommandDTO;
    }

    public void setDevopsHostCommandDTO(DevopsHostCommandDTO devopsHostCommandDTO) {
        this.devopsHostCommandDTO = devopsHostCommandDTO;
    }

    public List<DockerPortMapping> getPortMappingList() {
        return portMappingList;
    }

    public void setPortMappingList(List<DockerPortMapping> portMappingList) {
        this.portMappingList = portMappingList;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
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


    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

}
