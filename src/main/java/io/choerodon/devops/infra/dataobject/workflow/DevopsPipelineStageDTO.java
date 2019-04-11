package io.choerodon.devops.infra.dataobject.workflow;

import java.util.List;

/**
 * Created by Sheep on 2019/4/2.
 */
public class DevopsPipelineStageDTO {

    private Long stageId;
    private List<DevopsPipelineTaskDTO> tasks;
    private Boolean parallel;
    private List<String> usernames;
    private String nextStageTriggerType;
    private Boolean isMultiAssign;


    public List<DevopsPipelineTaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<DevopsPipelineTaskDTO> tasks) {
        this.tasks = tasks;
    }

    public String getNextStageTriggerType() {
        return nextStageTriggerType;
    }

    public void setNextStageTriggerType(String nextStageTriggerType) {
        this.nextStageTriggerType = nextStageTriggerType;
    }

    public Boolean getParallel() {
        return parallel;
    }

    public void setParallel(Boolean parallel) {
        this.parallel = parallel;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public Boolean getMultiAssign() {
        return isMultiAssign;
    }

    public void setMultiAssign(Boolean multiAssign) {
        isMultiAssign = multiAssign;
    }
}
