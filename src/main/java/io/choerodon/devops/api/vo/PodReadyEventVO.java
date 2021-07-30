package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/29 10:17
 */
public class PodReadyEventVO {
    private Long envId;
    private String instanceCode;
    private Long commandId;

    public PodReadyEventVO() {
    }

    public PodReadyEventVO(Long envId, String instanceCode, Long commandId) {
        this.envId = envId;
        this.instanceCode = instanceCode;
        this.commandId = commandId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getInstanceCode() {
        return instanceCode;
    }

    public void setInstanceCode(String instanceCode) {
        this.instanceCode = instanceCode;
    }
}
