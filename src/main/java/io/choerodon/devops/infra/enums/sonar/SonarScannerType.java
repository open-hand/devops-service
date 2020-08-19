package io.choerodon.devops.infra.enums.sonar;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/8/19 11:36
 */
public enum SonarScannerType {
    SONAR_SCANNER("SonarScanner"),
    SONAR_MAVEN("SonarMaven");

    private String value;

    SonarScannerType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
