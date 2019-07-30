package io.choerodon.devops.api.vo;

import javax.persistence.Transient;

/**
 * Created by Sheep on 2019/7/29.
 */
public class DevopsDeployRecordVO {

    private Long id;
    private Long projectId;
    private String deployType;
    private Long deployId;
    private String deployStatus;
    private String pipelineName;
    private String pipelineTriggerType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Long getDeployId() {
        return deployId;
    }

    public void setDeployId(Long deployId) {
        this.deployId = deployId;
    }

    public String getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(String deployStatus) {
        this.deployStatus = deployStatus;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getPipelineTriggerType() {
        return pipelineTriggerType;
    }

    public void setPipelineTriggerType(String pipelineTriggerType) {
        this.pipelineTriggerType = pipelineTriggerType;
    }
}
