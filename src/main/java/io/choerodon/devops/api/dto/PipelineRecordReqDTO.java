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
    private String pipelineName;
    private String triggerType;
    private Long projectId;
    private Date creationDate;
    private IamUserDTO userDTO;
    private String status;
    private Boolean execute;
    private List<PipelineStageRecordDTO> stageRecordDTOS;

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

    public List<PipelineStageRecordDTO> getStageRecordDTOS() {
        return stageRecordDTOS;
    }

    public void setStageRecordDTOS(List<PipelineStageRecordDTO> stageRecordDTOS) {
        this.stageRecordDTOS = stageRecordDTOS;
    }

    public Boolean getExecute() {
        return execute;
    }

    public void setExecute(Boolean execute) {
        this.execute = execute;
    }
}
