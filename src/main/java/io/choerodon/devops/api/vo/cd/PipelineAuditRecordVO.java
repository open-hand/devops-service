package io.choerodon.devops.api.vo.cd;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author
 * @since 2022-11-24 16:56:20
 */
public class PipelineAuditRecordVO {


    private Long id;
    @ApiModelProperty(value = "devops_pipeline_job_record.id", required = true)
    private Long jobRecordId;
    @ApiModelProperty(value = "是否会签 1是会签,0 是或签", required = true)
    private Boolean countersigned;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobRecordId() {
        return jobRecordId;
    }

    public void setJobRecordId(Long jobRecordId) {
        this.jobRecordId = jobRecordId;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }
}
