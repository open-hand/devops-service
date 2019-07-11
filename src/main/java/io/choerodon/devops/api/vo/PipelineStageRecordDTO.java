package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:12 2019/4/4
 * Description:
 */
public class PipelineStageRecordDTO {
    private Long id;
    private String stageName;
    private String status;
    private String triggerType;
    private Long pipelineId;
    private Integer isParallel;
    private String executionTime;
    private Long stageId;
    private List<PipelineTaskRecordDTO> taskRecordDTOS;
    private List<IamUserDTO> userDTOS;
    private Boolean index;

    public Boolean getIndex() {
        return index;
    }

    public void setIndex(Boolean index) {
        this.index = index;
    }

    public List<IamUserDTO> getUserDTOS() {
        return userDTOS;
    }

    public void setUserDTOS(List<IamUserDTO> userDTOS) {
        this.userDTOS = userDTOS;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Integer getIsParallel() {
        return isParallel;
    }

    public void setIsParallel(Integer isParallel) {
        this.isParallel = isParallel;
    }

    public List<PipelineTaskRecordDTO> getTaskRecordDTOS() {
        return taskRecordDTOS;
    }

    public void setTaskRecordDTOS(List<PipelineTaskRecordDTO> taskRecordDTOS) {
        this.taskRecordDTOS = taskRecordDTOS;
    }
}
