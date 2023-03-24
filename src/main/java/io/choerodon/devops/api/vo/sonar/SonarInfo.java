package io.choerodon.devops.api.vo.sonar;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/14 15:02
 */
public class SonarInfo {
    private String sonarUrl;
    private String sonarToken;

    private String sonarKeyPrefix;

    public SonarInfo() {
    }

    public SonarInfo(String sonarUrl, String sonarToken, String sonarKeyPrefix) {
        this.sonarUrl = sonarUrl;
        this.sonarToken = sonarToken;
        this.sonarKeyPrefix = sonarKeyPrefix;
    }

    public String getSonarUrl() {
        return sonarUrl;
    }

    public void setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    public String getSonarToken() {
        return sonarToken;
    }

    public void setSonarToken(String sonarToken) {
        this.sonarToken = sonarToken;
    }

    public String getSonarKeyPrefix() {
        return sonarKeyPrefix;
    }

    public void setSonarKeyPrefix(String sonarKeyPrefix) {
        this.sonarKeyPrefix = sonarKeyPrefix;
    }
}
