package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/29 10:17
 */
public class PodReadyEventVO {
    private Long instanceId;
    private Long commandId;

    public PodReadyEventVO() {
    }

    public PodReadyEventVO(Long instanceId, Long commandId) {
        this.instanceId = instanceId;
        this.commandId = commandId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

}
