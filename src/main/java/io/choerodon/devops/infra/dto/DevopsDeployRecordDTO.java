package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by Sheep on 2019/7/29.
 */

@Table(name="devops_deploy_record")
public class DevopsDeployRecordDTO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long projectId;
    private String deployType;
    private Long deployId;

    @Transient
    private String deployStatus;
    @Transient
    private String pipelineName;
    @Transient
    private String pipelineTriggerType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
