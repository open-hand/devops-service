package io.choerodon.devops.infra.dataobject;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:29 2019/4/3
 * Description:
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_pipeline_stage_record")
public class PipelineStageRecordDO {
    @Id
    @GeneratedValue
    private Long id;
    private Long pipelineRecordId;
    private String status;
    private String triggerType;
    private Integer isParallel;
    private Date executionTime;
    private Long projectId;

    public PipelineStageRecordDO(Long projectId, Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
        this.projectId = projectId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getIsParallel() {
        return isParallel;
    }

    public void setIsParallel(Integer isParallel) {
        this.isParallel = isParallel;
    }

}
