package io.choerodon.devops.app.eventhandler.payload;

public class DeleteHelmHookJobRequest {
    private String namespace;
    private String jobName;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
