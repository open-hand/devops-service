package io.choerodon.devops.api.vo.host;

import java.util.Date;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 〈功能简述〉
 * 〈java进程信息VO〉
 *
 * @author wanghao
 * @Date 2021/6/25 17:35
 */
public class InstanceProcessInfoVO {
    private String instanceId;
    private Boolean ready;
    private IamUserDTO deployer;
    private Date deployDate;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
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
