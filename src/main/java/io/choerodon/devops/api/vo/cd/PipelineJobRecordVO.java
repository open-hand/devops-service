package io.choerodon.devops.api.vo.cd;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.pipeline.Audit;
import io.choerodon.devops.api.vo.pipeline.DeployInfo;

/**
 * @author hao.wang@zknow.com
 * @since 2022-12-02 11:12:13
 */
public class PipelineJobRecordVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "名称", required = true)
    private String name;
    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    private Long pipelineId;
    @ApiModelProperty(value = "所属任务Id,devops_pipeline_job.id", required = true)
    @Encrypt
    private Long jobId;
    @ApiModelProperty(value = "关联流水线记录Id,devops_pipeline_record.id", required = true)
    private Long pipelineRecordId;
    @ApiModelProperty(value = "关联阶段记录Id,devops_pipeline_stage_record.id", required = true)
    @Encrypt
    private Long stageRecordId;
    @ApiModelProperty(value = "状态", required = true)
    private String status;
    @ApiModelProperty(value = "任务开始时间", required = true)
    private Date startedDate;
    @ApiModelProperty(value = "任务结束时间", required = true)
    private Date finishedDate;
    @ApiModelProperty(value = "任务类型", required = true)
    private String type;
    @Encrypt
    private Long commandId;

    @ApiModelProperty("人工卡点任务信息")
    private Audit audit;

    @ApiModelProperty("chart部署任务信息")
    private DeployInfo deployInfo;

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public DeployInfo getDeployInfo() {
        return deployInfo;
    }

    public void setDeployInfo(DeployInfo deployInfo) {
        this.deployInfo = deployInfo;
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

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
}
