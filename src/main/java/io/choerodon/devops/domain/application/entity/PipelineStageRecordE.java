package io.choerodon.devops.domain.application.entity;

import java.util.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:06 2019/4/4
 * Description:
 */
public class PipelineStageRecordE {
    private Long id;
    private String stageName;
    private String triggerType;
    private Integer isParallel;
    private Long projectId;
    private Long pipelineRecordId;
    private Date executionTime;
    private String status;
    private Long stageId;
    private Long objectVersionNumber;

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public PipelineStageRecordE() {
    }

    public PipelineStageRecordE(String triggerType, Integer isParallel, Long projectId, Long pipelineRecordId) {
        this.triggerType = triggerType;
        this.isParallel = isParallel;
        this.projectId = projectId;
        this.pipelineRecordId = pipelineRecordId;
    }

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
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

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getIsParallel() {
        return isParallel;
    }

    public void setIsParallel(Integer isParallel) {
        this.isParallel = isParallel;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
