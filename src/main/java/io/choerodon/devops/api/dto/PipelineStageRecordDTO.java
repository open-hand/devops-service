package io.choerodon.devops.api.dto;

import oracle.jdbc.driver.DatabaseError;

import java.util.Date;
import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:12 2019/4/4
 * Description:
 */
public class PipelineStageRecordDTO {
    private Long id;
    private Long stageName;
    private String status;
    private String triggerType;
    private String triggerUserName;
    private Long triggerUserId;
    private Long pipelineId;
    private Integer isParallel;
    private Date executionTime;
    private List<PipelineTaskRecordDTO> taskRecordDTOS;

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStageName() {
        return stageName;
    }

    public void setStageName(Long stageName) {
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

    public String getTriggerUserName() {
        return triggerUserName;
    }

    public void setTriggerUserName(String triggerUserName) {
        this.triggerUserName = triggerUserName;
    }

    public Long getTriggerUserId() {
        return triggerUserId;
    }

    public void setTriggerUserId(Long triggerUserId) {
        this.triggerUserId = triggerUserId;
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
