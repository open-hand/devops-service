package io.choerodon.devops.domain.application.entity;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:28 2019/4/4
 * Description:
 */
public class PipelineRecordE {
    private Long id;
    private String name;
    private String triggerType;
    private Long projectId;
    private Long pipelineId;
    private String status;
    private String processInstanceId;

    public PipelineRecordE(Long pipelineId, String triggerType, Long projectId) {
        this.pipelineId = pipelineId;
        this.triggerType = triggerType;
        this.projectId = projectId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public PipelineRecordE() {

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
