package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  21:04 2019/4/9
 * Description:
 */
public class PipelineRecordReqVO {
    private Long id;
    private String pipelineName;
    private String triggerType;
    private Long projectId;
    private Date creationDate;
    private IamUserDTO userDTO;
    private String status;
    private Boolean execute;
    private String type;
    private Long stageRecordId;
    private Long taskRecordId;
    private String stageName;
    private List<PipelineStageRecordVO> stageRecordDTOS;

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public Long getTaskRecordId() {
        return taskRecordId;
    }

    public void setTaskRecordId(Long taskRecordId) {
        this.taskRecordId = taskRecordId;
    }

    public IamUserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(IamUserDTO userDTO) {
        this.userDTO = userDTO;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PipelineStageRecordVO> getStageRecordDTOS() {
        return stageRecordDTOS;
    }

    public void setStageRecordDTOS(List<PipelineStageRecordVO> stageRecordDTOS) {
        this.stageRecordDTOS = stageRecordDTOS;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }
}
