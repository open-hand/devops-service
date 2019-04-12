package io.choerodon.devops.infra.dataobject.workflow;

import java.util.List;
import java.util.Map;


/**
 * Created by Sheep on 2019/4/2.
 */
public class DevopsPipelineTaskDTO {

    private String taskName;
    private List<String> usernames;
    private String taskType;
    private Boolean isMultiAssign;
    private Map<String, Object> params;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Boolean getMultiAssign() {
        return isMultiAssign;
    }

    public void setMultiAssign(Boolean multiAssign) {
        isMultiAssign = multiAssign;
    }
}
