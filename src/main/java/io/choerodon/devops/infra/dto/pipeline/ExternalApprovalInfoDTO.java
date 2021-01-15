package io.choerodon.devops.infra.dto.pipeline;


/**
 * 〈功能简述〉
 *
 *
 * @author wanghao
 * @since 2020/12/9 11:32
 */
public class ExternalApprovalInfoDTO {
    private Long projectId;
//    private Long pipelineId;
//    private Long stageId;
//    private Long jobId;
    private Long pipelineRecordId;
    private Long stageRecordId;
    private Long jobRecordId;
    private String description;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

//    public Long getPipelineId() {
//        return pipelineId;
//    }
//
//    public void setPipelineId(Long pipelineId) {
//        this.pipelineId = pipelineId;
//    }
//
//    public Long getStageId() {
//        return stageId;
//    }
//
//    public void setStageId(Long stageId) {
//        this.stageId = stageId;
//    }
//
//    public Long getJobId() {
//        return jobId;
//    }
//
//    public void setJobId(Long jobId) {
//        this.jobId = jobId;
//    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
