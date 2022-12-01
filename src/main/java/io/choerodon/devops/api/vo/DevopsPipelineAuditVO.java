package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsPipelineAuditVO {
    @ApiModelProperty("任务名称")
    private String jobName;
    @Encrypt
    @ApiModelProperty("任务记录id")
    private Long jobRecordId;


    public DevopsPipelineAuditVO() {
    }

    public DevopsPipelineAuditVO(String jobName, Long jobRecordId) {
        this.jobName = jobName;
        this.jobRecordId = jobRecordId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Long getJobRecordId() {
        return jobRecordId;
    }

    public void setJobRecordId(Long jobRecordId) {
        this.jobRecordId = jobRecordId;
    }
}
