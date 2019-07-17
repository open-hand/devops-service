package io.choerodon.devops.api.vo;

/**
 * Creator: Runge
 * Date: 2018/4/28
 * Time: 09:55
 * Description:
 */
public class EnvInstancesVO {
    private Long instanceId;
    private String instanceName;
    private String instanceStatus;

    public EnvInstancesVO() {
    }

    /**
     * 构造函数
     */
    public EnvInstancesVO(Long instanceId, String instanceName, String instanceStatus) {
        this.instanceId = instanceId;
        this.instanceName = instanceName;
        this.instanceStatus = instanceStatus;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }
}
