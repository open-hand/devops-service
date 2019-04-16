package io.choerodon.devops.domain.application.entity;

import java.sql.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:36 2019/4/9
 * Description:
 */
public class PipelineTaskRecordE {
    private Long id;
    private String name;
    private Long stageRecordId;
    private String taskType;
    private String status;
    private String triggerVersion;
    private Long applicationId;
    private Long envId;
    private Long instanceId;
    private Long versionId;
    private Long projectId;
    private Long appDeployId;
    private Date executionTime;
    private Integer isCountersigned;
    private String value;
    private Long taskId;
    private String appName;
    private String envName;
    private String version;
    private String instanceName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public PipelineTaskRecordE() {
    }

    public PipelineTaskRecordE(Long stageRecordId, String taskType, Integer isCountersigned) {
        this.stageRecordId = stageRecordId;
        this.taskType = taskType;
        this.isCountersigned = isCountersigned;
    }
    public PipelineTaskRecordE(Long instanceId, String status) {
        this.instanceId = instanceId;
        this.status = status;
    }

    public PipelineTaskRecordE(Long stageRecordId, String taskType, String triggerVersion, Long applicationId, Long envId, Long instanceId, String value) {
        this.stageRecordId = stageRecordId;
        this.taskType = taskType;
        this.triggerVersion = triggerVersion;
        this.applicationId = applicationId;
        this.envId = envId;
        this.instanceId = instanceId;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTriggerVersion() {
        return triggerVersion;
    }

    public void setTriggerVersion(String triggerVersion) {
        this.triggerVersion = triggerVersion;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAppDeployId() {
        return appDeployId;
    }

    public void setAppDeployId(Long appDeployId) {
        this.appDeployId = appDeployId;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public Integer getIsCountersigned() {
        return isCountersigned;
    }

    public void setIsCountersigned(Integer isCountersigned) {
        this.isCountersigned = isCountersigned;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
