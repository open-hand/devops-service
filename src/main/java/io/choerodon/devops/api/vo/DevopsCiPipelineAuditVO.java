package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsCiPipelineAuditVO {
    @ApiModelProperty("任务名称")
    private String jobName;
    @Encrypt
    @ApiModelProperty("任务记录id")
    private Long jobRecordId;
    @ApiModelProperty("判断当前用户能否进行审核，能否看到人工审核这个操作按钮")
    private Boolean execute;


    public DevopsCiPipelineAuditVO() {
    }

    public DevopsCiPipelineAuditVO(String jobName, Long jobRecordId, Boolean execute) {
        this.jobName = jobName;
        this.jobRecordId = jobRecordId;
        this.execute = execute;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
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
