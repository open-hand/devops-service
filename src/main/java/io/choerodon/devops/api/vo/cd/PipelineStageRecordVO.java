package io.choerodon.devops.api.vo.cd;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author hao.wang@zknow.com
 * @since 2022-12-02 10:57:55
 */
public class PipelineStageRecordVO {


    List<PipelineJobRecordVO> jobRecordList;
    @ApiModelProperty(value = "名称", required = true)
    private String name;
    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    private Long pipelineId;
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "阶段顺序", required = true)
    private Integer sequence;
    @ApiModelProperty(value = "所属阶段Id,devops_pipeline_stage.id", required = true)
    @Encrypt
    private Long stageId;
    @ApiModelProperty(value = "关联流水线记录Id,devops_pipeline_record.id", required = true)
    private Long pipelineRecordId;
    @ApiModelProperty(value = "状态", required = true)
    private String status;
    @ApiModelProperty(value = "下一阶段id", required = true)
    @Encrypt
    private Long nextStageRecordId;
    public List<PipelineJobRecordVO> getJobRecordList() {
        return jobRecordList;
    }

    public void setJobRecordList(List<PipelineJobRecordVO> jobRecordList) {
        this.jobRecordList = jobRecordList;
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

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Long getNextStageRecordId() {
        return nextStageRecordId;
    }

    public void setNextStageRecordId(Long nextStageRecordId) {
        this.nextStageRecordId = nextStageRecordId;
    }

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
