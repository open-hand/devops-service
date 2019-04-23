package io.choerodon.devops.domain.application.entity;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:19 2019/4/9
 * Description:
 */
public class PipelineUserRecordRelE {
    private Long id;
    private Long userId;
    private Long pipelineRecordId;
    private Long stageRecordId;
    private Long taskRecordId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
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
}
