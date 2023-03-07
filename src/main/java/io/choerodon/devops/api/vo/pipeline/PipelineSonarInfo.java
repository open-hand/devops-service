package io.choerodon.devops.api.vo.pipeline;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.SonarContentVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/16 21:47
 */
public class PipelineSonarInfo {
    private String scannerType;
    @ApiModelProperty("ci中返回sonar")
    private List<SonarContentVO> sonarContentVOS;
    private DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO;

    public PipelineSonarInfo(String scannerType, List<SonarContentVO> sonarContentVOS, DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO) {
        this.scannerType = scannerType;
        this.sonarContentVOS = sonarContentVOS;
        this.devopsCiSonarQualityGateVO = devopsCiSonarQualityGateVO;
    }

    public DevopsCiSonarQualityGateVO getDevopsCiSonarQualityGateVO() {
        return devopsCiSonarQualityGateVO;
    }

    public void setDevopsCiSonarQualityGateVO(DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO) {
        this.devopsCiSonarQualityGateVO = devopsCiSonarQualityGateVO;
    }

    public String getScannerType() {
        return scannerType;
    }

    public void setScannerType(String scannerType) {
        this.scannerType = scannerType;
    }

    public List<SonarContentVO> getSonarContentVOS() {
        return sonarContentVOS;
    }

    public void setSonarContentVOS(List<SonarContentVO> sonarContentVOS) {
        this.sonarContentVOS = sonarContentVOS;
    }
}
