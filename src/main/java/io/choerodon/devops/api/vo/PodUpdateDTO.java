package io.choerodon.devops.api.vo;

public class PodUpdateDTO {
    String releaseNames;
    String podName;
    String conName;
    Long status;
    String logFile;

    public String getReleaseNames() {
        return releaseNames;
    }

    public void setReleaseNames(String releaseNames) {
        this.releaseNames = releaseNames;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getConName() {
        return conName;
    }

    public void setConName(String conName) {
        this.conName = conName;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }
}
