package io.choerodon.devops.app.eventhandler.payload;

/**
 * @author scp
 * @date 2020/7/11
 * @description
 */
public class HostDeployPayload {
    private Long pipelineRecordId;
    private Long stageRecordId;
    private Long jobRecordId;

    public HostDeployPayload() {
    }

    public HostDeployPayload(Long pipelineRecordId, Long stageRecordId, Long jobRecordId) {
        this.pipelineRecordId = pipelineRecordId;
        this.stageRecordId = stageRecordId;
        this.jobRecordId = jobRecordId;
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

    public Long getJobRecordId() {
        return jobRecordId;
    }

    public void setJobRecordId(Long jobRecordId) {
        this.jobRecordId = jobRecordId;
    }
}
