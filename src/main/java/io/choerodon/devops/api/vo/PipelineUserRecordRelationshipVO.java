package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:37 2019/4/12
 * Description:
 */
public class PipelineUserRecordRelationshipVO {
    private Long userId;
    private Long pipelineRecordId;
    private Long stageRecordId;
    private Long taskRecordId;
    private String type;
    private Boolean isApprove;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsApprove() {
        return isApprove;
    }

    public void setIsApprove(Boolean isApprove) {
        this.isApprove = isApprove;
    }
}
