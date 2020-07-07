package io.choerodon.devops.api.vo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class DevopsCdStageRecordVO {
    private Long id;
    private String name;
    private Long sequence;
    private String status;
    private Long durationSeconds;
    private List<DevopsCdJobRecordVO> devopsCdJobRecordVOS;
    private String triggerType;
    private Long pipelineId;
    private Boolean parallel;
    private Long stageId;
    private List<PipelineUserVO> userDTOS;
    private Boolean index;

    private Date startedDate;
    private Date finishedDate;
    private Long executionTime;

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public List<DevopsCdJobRecordVO> getDevopsCdJobRecordVOS() {
        return devopsCdJobRecordVOS;
    }

    public void setDevopsCdJobRecordVOS(List<DevopsCdJobRecordVO> devopsCdJobRecordVOS) {
        this.devopsCdJobRecordVOS = devopsCdJobRecordVOS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
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

    public Boolean getParallel() {
        return parallel;
    }

    public void setParallel(Boolean parallel) {
        this.parallel = parallel;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public List<PipelineUserVO> getUserDTOS() {
        return userDTOS;
    }

    public void setUserDTOS(List<PipelineUserVO> userDTOS) {
        this.userDTOS = userDTOS;
    }

    public Boolean getIndex() {
        return index;
    }

    public void setIndex(Boolean index) {
        this.index = index;
    }

    public void setStageExecuteTime() {
        LocalDateTime start = startedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime finished = finishedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.executionTime = Duration.between(start, finished).getSeconds();
    }
}
