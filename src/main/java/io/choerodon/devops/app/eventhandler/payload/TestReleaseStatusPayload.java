package io.choerodon.devops.app.eventhandler.payload;

public class TestReleaseStatusPayload {

    String releaseName;
    String status;

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
