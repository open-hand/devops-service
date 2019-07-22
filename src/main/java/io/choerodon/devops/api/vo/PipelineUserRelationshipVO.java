package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:30 2019/4/8
 * Description:
 */
public class PipelineUserRelationshipVO {
    private Long id;
    private Long userId;
    private Long pipelineId;
    private Long stageId;
    private Long taskId;

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

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
