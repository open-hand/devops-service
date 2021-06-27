package io.choerodon.devops.api.vo.host;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

import java.util.Date;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/27 20:41
 */
public class DockerProcessInfoVO {
    private String containerId;
    private String image;
    private String name;
    private String port;
    private IamUserDTO deployer;
    private Date deployDate;


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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public IamUserDTO getDeployer() {
        return deployer;
    }

    public void setDeployer(IamUserDTO deployer) {
        this.deployer = deployer;
    }

    public Date getDeployDate() {
        return deployDate;
    }

    public void setDeployDate(Date deployDate) {
        this.deployDate = deployDate;
    }
}
