package io.choerodon.devops.domain.application.entity;

import java.util.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:28 2019/4/4
 * Description:
 */
public class PipelineRecordE {
    private Long id;
    private String pipelineName;
    private String triggerType;
    private Long projectId;
    private Long pipelineId;
    private String status;
    private String bpmDefinition;
    private Date lastUpdateDate;
    private Date creationDate;
    private String businessKey;

    public PipelineRecordE(Long pipelineId, String triggerType, Long projectId, String status, String pipelineName) {
        this.pipelineId = pipelineId;
        this.triggerType = triggerType;
        this.projectId = projectId;
        this.status = status;
        this.pipelineName = pipelineName;
    }

    public PipelineRecordE() {

    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getBpmDefinition() {
        return bpmDefinition;
    }

    public void setBpmDefinition(String bpmDefinition) {
        this.bpmDefinition = bpmDefinition;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
