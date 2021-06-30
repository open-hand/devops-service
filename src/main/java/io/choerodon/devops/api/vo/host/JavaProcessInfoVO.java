package io.choerodon.devops.api.vo.host;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

import java.util.Date;

/**
 * 〈功能简述〉
 * 〈java进程信息VO〉
 *
 * @author wanghao
 * @Date 2021/6/25 17:35
 */
public class JavaProcessInfoVO {
    private Long instanceId;
    private String pid;
    private String port;
    private IamUserDTO deployer;
    private Date deployDate;

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
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
