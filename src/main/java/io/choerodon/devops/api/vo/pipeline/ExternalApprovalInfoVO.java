package io.choerodon.devops.api.vo.pipeline;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;

/**
 * 〈功能简述〉
 * 〈外部卡点任务metadataVO〉
 *
 * @author wanghao
 * @since 2020/12/9 11:32
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalApprovalInfoVO {

    private Long projectId;
    private Long pipelineRecordId;
    private Long stageRecordId;
    private Long jobRecordId;
    @ApiModelProperty("外部卡点任务回调认证token")
    private String callbackToken;

    private DevopsCdJobRecordDTO currentCdJob;

    private CiCdPipelineRecordVO pipelineRecordDetails;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public DevopsCdJobRecordDTO getCurrentCdJob() {
        return currentCdJob;
    }

    public void setCurrentCdJob(DevopsCdJobRecordDTO currentCdJob) {
        this.currentCdJob = currentCdJob;
    }

    public CiCdPipelineRecordVO getPipelineRecordDetails() {
        return pipelineRecordDetails;
    }

    public void setPipelineRecordDetails(CiCdPipelineRecordVO pipelineRecordDetails) {
        this.pipelineRecordDetails = pipelineRecordDetails;
    }

    public String getCallbackToken() {
        return callbackToken;
    }

    public void setCallbackToken(String callbackToken) {
        this.callbackToken = callbackToken;
    }

    @Override
    public String toString() {
        return "ExternalApprovalInfoVO{" +
                "projectId=" + projectId +
                ", pipelineRecordId=" + pipelineRecordId +
                ", stageRecordId=" + stageRecordId +
                ", jobRecordId=" + jobRecordId +
                ", callbackToken='" + callbackToken + '\'' +
                ", currentCdJob=" + currentCdJob +
                ", pipelineRecordDetails=" + pipelineRecordDetails +
                '}';
    }
}
