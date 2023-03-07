package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 表示一个流水线对于实例的引用的位置 (删除实例或者停用实例时需要)
 *
 * @author zmf
 * @since 2021/3/17
 */
public class PipelineInstanceReferenceVO {
    @ApiModelProperty("流程类型/ci、cd")
    private String type;
    @ApiModelProperty("任务id")
    private Long jobId;
    @ApiModelProperty("任务id")
    private Long taskId;
    @ApiModelProperty("流水线的名称")
    private String pipelineName;
    @ApiModelProperty("阶段名称")
    private String stageName;
    @ApiModelProperty("任务名称")
    private String jobName;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PipelineInstanceReferenceVO() {
    }

    public PipelineInstanceReferenceVO(String pipelineName, String stageName, String jobName) {
        this.pipelineName = pipelineName;
        this.stageName = stageName;
        this.jobName = jobName;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
