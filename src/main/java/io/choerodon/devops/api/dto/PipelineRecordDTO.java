package io.choerodon.devops.api.dto;

import java.util.Date;
import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:09 2019/4/4
 * Description:
 */
public class PipelineRecordDTO {
    private Long id;
    private String status;
    private String triggerType;
    private Long projectId;
    private Date lastUpdateDate;
    private List<PipelineStageRecordDTO> stageDTOList;
    private String processInstanceId;

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public List<PipelineStageRecordDTO> getStageDTOList() {
        return stageDTOList;
    }

    public void setStageDTOList(List<PipelineStageRecordDTO> stageDTOList) {
        this.stageDTOList = stageDTOList;
    }
}
