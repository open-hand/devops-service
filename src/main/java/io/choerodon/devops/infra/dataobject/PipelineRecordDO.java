package io.choerodon.devops.infra.dataobject;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:23 2019/4/3
 * Description:
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_pipeline_record")
public class PipelineRecordDO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;
    private Long pipelineId;
    private String status;
    private String trigger_type;
    private Date executionTime;
    private String bpmDefinition;

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

    public String getTrigger_type() {
        return trigger_type;
    }

    public void setTrigger_type(String trigger_type) {
        this.trigger_type = trigger_type;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public String getBpmDefinition() {
        return bpmDefinition;
    }

    public void setBpmDefinition(String bpmDefinition) {
        this.bpmDefinition = bpmDefinition;
    }
}
