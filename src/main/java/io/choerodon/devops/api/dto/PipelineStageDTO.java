package io.choerodon.devops.api.dto;

import java.util.Date;
import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:20 2019/4/8
 * Description:
 */
public class PipelineStageDTO {
    private Long id;
    private String stageName;
    private String triggerType;
    private List<Long> stageUserRelDTOS;
    private Integer isParallel;
    private List<PipelineTaskDTO> pipelineTaskDTOS;
    private Long pipelineId;
    private Long projectId;
    private Long objectVersionNumber;
    private Date lastUpdateDate;

    public List<Long> getStageUserRelDTOS() {
        return stageUserRelDTOS;
    }

    public void setStageUserRelDTOS(List<Long> stageUserRelDTOS) {
        this.stageUserRelDTOS = stageUserRelDTOS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
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

    public List<PipelineTaskDTO> getPipelineTaskDTOS() {
        return pipelineTaskDTOS;
    }

    public void setPipelineTaskDTOS(List<PipelineTaskDTO> pipelineTaskDTOS) {
        this.pipelineTaskDTOS = pipelineTaskDTOS;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}
