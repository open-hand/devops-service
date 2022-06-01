package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsCdPipelineDeatilVO {
    @ApiModelProperty("阶段名称")
    private String stageName;
    @Encrypt
    @ApiModelProperty("阶段记录id")
    private Long stageRecordId;
    @Encrypt
    @ApiModelProperty("任务记录id")
    private Long taskRecordId;
    @ApiModelProperty("审核类型")
    private String type;
    @ApiModelProperty("判断当前用户能否进行审核，能否看到人工审核这个操作按钮")
    private Boolean execute;

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public Long getTaskRecordId() {
        return taskRecordId;
    }

    public void setTaskRecordId(Long taskRecordId) {
        this.taskRecordId = taskRecordId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DevopsCdPipelineDeatilVO{" +
                "stageName='" + stageName + '\'' +
                ", stageRecordId=" + stageRecordId +
                ", taskRecordId=" + taskRecordId +
                ", type='" + type + '\'' +
                ", execute=" + execute +
                '}';
    }
}
