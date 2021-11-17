package io.choerodon.devops.api.vo;

public class DevopsCiJobLogVO {
    private Boolean endFlag;
    private String logs;

    public DevopsCiJobLogVO(Boolean endFlag, String logs) {
        this.endFlag = endFlag;
        this.logs = logs;
    }

    public Boolean getEndFlag() {
        return endFlag;
    }

    public void setEndFlag(Boolean endFlag) {
        this.endFlag = endFlag;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }
}
