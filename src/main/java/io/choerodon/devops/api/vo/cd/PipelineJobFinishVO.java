package io.choerodon.devops.api.vo.cd;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/12/2 17:40
 */
public class PipelineJobFinishVO {
    private Long stageRecordId;
    private Long jobRecordId;

    public PipelineJobFinishVO() {
    }

    public PipelineJobFinishVO(Long stageRecordId, Long jobRecordId) {
        this.stageRecordId = stageRecordId;
        this.jobRecordId = jobRecordId;
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
