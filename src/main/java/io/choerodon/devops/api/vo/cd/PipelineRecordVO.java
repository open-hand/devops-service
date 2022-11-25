package io.choerodon.devops.api.vo.cd;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author
 * @since 2022-11-25 14:59:00
 */
public class PipelineRecordVO {


    private Long id;
    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    private Long pipelineId;
    @ApiModelProperty(value = "状态", required = true)
    private String status;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
