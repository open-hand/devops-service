package io.choerodon.devops.api.vo.test;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/1/12 10:02
 */
public class ApiTestCompleteEventVO {

    @ApiModelProperty(name = "测试任务记录id")
    @Encrypt
    private Long taskRecordId;
    @ApiModelProperty(name = "测试任务触发类型")
    private String triggerType;
    @ApiModelProperty(name = "测试任务触发者id")
    @Encrypt
    private Long triggerId;
    @ApiModelProperty(name = "测试任务执行状态")
    private String status;

    public Long getTaskRecordId() {
        return taskRecordId;
    }

    public void setTaskRecordId(Long taskRecordId) {
        this.taskRecordId = taskRecordId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ApiTestCompleteEventVO{" +
                "taskRecordId=" + taskRecordId +
                ", triggerType='" + triggerType + '\'' +
                ", triggerId=" + triggerId +
                ", status='" + status + '\'' +
                '}';
    }
}
