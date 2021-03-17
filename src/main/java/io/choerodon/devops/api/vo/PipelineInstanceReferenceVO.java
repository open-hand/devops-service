package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 表示一个流水线对于实例的引用的位置 (删除实例或者停用实例时需要)
 *
 * @author zmf
 * @since 2021/3/17
 */
public class PipelineInstanceReferenceVO {
    @ApiModelProperty("流水线的名称")
    private String pipelineName;
    @ApiModelProperty("阶段名称")
    private String stageName;
    @ApiModelProperty("任务名称")
    private String jobName;

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
