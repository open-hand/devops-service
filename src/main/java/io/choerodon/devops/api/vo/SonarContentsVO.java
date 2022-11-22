package io.choerodon.devops.api.vo;

import java.util.List;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;

/**
 * Created by Sheep on 2019/5/7.
 */
public class SonarContentsVO {
    private String status;
    private String date;
    private String mode;
    private String parameter;
    private List<SonarContentVO> sonarContents;
    private DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<SonarContentVO> getSonarContents() {
        return sonarContents;
    }

    public void setSonarContents(List<SonarContentVO> sonarContents) {
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

    public DevopsCiSonarQualityGateVO getDevopsCiSonarQualityGateVO() {
        return devopsCiSonarQualityGateVO;
    }

    public void setDevopsCiSonarQualityGateVO(DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO) {
        this.devopsCiSonarQualityGateVO = devopsCiSonarQualityGateVO;
    }
}
