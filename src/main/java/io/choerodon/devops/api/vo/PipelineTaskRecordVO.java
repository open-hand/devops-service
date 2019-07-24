package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:34 2019/4/14
 * Description:
 */
public class PipelineTaskRecordVO {
    private Long id;
    private String name;
    private String status;
    private String taskType;
    private Integer isCountersigned;
    private String appName;
    private String envName;
    private Long applicationId;
    private Long envId;
    private String version;
    private String instanceName;
    private Long taskId;
    private String instanceStatus;
    private Long instanceId;
    private Boolean envPermission;
    private List<PipelineUserVO> userDTOList;

    public Boolean getEnvPermission() {
        return envPermission;
    }

    public void setEnvPermission(Boolean envPermission) {
        this.envPermission = envPermission;
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

    public List<PipelineUserVO> getUserDTOList() {
        return userDTOList;
    }

    public void setUserDTOList(List<PipelineUserVO> userDTOList) {
        this.userDTOList = userDTOList;
    }

    public String getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Integer getIsCountersigned() {
        return isCountersigned;
    }

    public void setIsCountersigned(Integer isCountersigned) {
        this.isCountersigned = isCountersigned;
    }

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
}
