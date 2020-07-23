package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsCdPipelineDeatilVO {
    private String stageName;
    @Encrypt
    private Long stageRecordId;
    @Encrypt
    private Long taskRecordId;
    private String type;
    //判断当前用户能否进行审核，能否看到人工审核这个操作按钮
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
}
