package io.choerodon.devops.api.vo;

import java.util.List;

public class DevopsCdStageRecordVO extends StageRecordVO {
    private Long id;
    private Long durationSeconds;
    private List<DevopsCdJobRecordVO> jobRecordVOList;
    private String triggerType;
    private Long pipelineId;
    private Boolean parallel;
    private Long stageId;
    private List<PipelineUserVO> userDTOS;
    private Boolean index;


    private Long executionTime;
    private Long sequence;


    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public List<DevopsCdJobRecordVO> getJobRecordVOList() {
        return jobRecordVOList;
    }

    public void setJobRecordVOList(List<DevopsCdJobRecordVO> jobRecordVOList) {
        this.jobRecordVOList = jobRecordVOList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


}
