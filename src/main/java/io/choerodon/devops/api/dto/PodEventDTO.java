package io.choerodon.devops.api.dto;

public class PodEventDTO {

    private String name;
    private String event;
    private String log;
    private String jobPodStatus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getJobPodStatus() {
        return jobPodStatus;
    }

    public void setJobPodStatus(String jobPodStatus) {
        this.jobPodStatus = jobPodStatus;
    }
}
