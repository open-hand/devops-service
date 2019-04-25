package io.choerodon.devops.api.dto;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:34 2019/4/14
 * Description:
 */
public class PipelineTaskRecordDTO {
    private Long id;
    private String name;
    private String status;
    private String taskType;
    private Integer isCountersigned;
    private String appName;
    private String envName;
    private String version;
    private String instanceName;
    private Long taskId;
    private List<IamUserDTO> userDTOList;

    public List<IamUserDTO> getUserDTOList() {
        return userDTOList;
    }

    public void setUserDTOList(List<IamUserDTO> userDTOList) {
        this.userDTOList = userDTOList;
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
