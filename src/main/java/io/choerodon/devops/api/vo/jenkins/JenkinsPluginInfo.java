package io.choerodon.devops.api.vo.jenkins;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/21 11:15
 */
public class JenkinsPluginInfo {
    private String status;

    private String version;

    private String lastedVersion;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastedVersion() {
        return lastedVersion;
    }

    public void setLastedVersion(String lastedVersion) {
        this.lastedVersion = lastedVersion;
    }
}
