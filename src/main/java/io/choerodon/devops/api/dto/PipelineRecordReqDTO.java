package io.choerodon.devops.api.dto;

import java.util.Date;
import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  21:04 2019/4/9
 * Description:
 */
public class PipelineRecordReqDTO {
    private Long id;
    private String name;
    private String triggerType;
    private Long projectId;
    private Date creationDate;
    private String triggerUserName;
    private Long triggerUserId;
    private String stauts;
    private List<PipelineStageRecordDTO> stageRecordDTOS;

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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getTriggerUserName() {
        return triggerUserName;
    }

    public void setTriggerUserName(String triggerUserName) {
        this.triggerUserName = triggerUserName;
    }

    public Long getTriggerUserId() {
        return triggerUserId;
    }

    public void setTriggerUserId(Long triggerUserId) {
        this.triggerUserId = triggerUserId;
    }

    public String getStauts() {
        return stauts;
    }

    public void setStauts(String stauts) {
        this.stauts = stauts;
    }

    public List<PipelineStageRecordDTO> getStageRecordDTOS() {
        return stageRecordDTOS;
    }

    public void setStageRecordDTOS(List<PipelineStageRecordDTO> stageRecordDTOS) {
        this.stageRecordDTOS = stageRecordDTOS;
    }
}
