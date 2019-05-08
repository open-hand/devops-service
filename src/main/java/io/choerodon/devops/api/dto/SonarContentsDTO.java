package io.choerodon.devops.api.dto;

import java.util.List;

/**
 * Created by Sheep on 2019/5/7.
 */
public class SonarContentsDTO {
    private String status;
    private String date;
    private String mode;
    private String parameter;
    private List<SonarContentDTO> sonarContents;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<SonarContentDTO> getSonarContents() {
        return sonarContents;
    }

    public void setSonarContents(List<SonarContentDTO> sonarContents) {
        this.sonarContents = sonarContents;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
