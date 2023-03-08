package io.choerodon.devops.api.vo.jenkins;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/7 10:06
 */
public class JenkinsBuildInfo {

    private String id;

    private String status;

    private Long startTimeMillis;
    private Long durationMillis;

    private String username;

    private String triggerType;

    private String branch;

    private String remoteUrl;

    private JenkinsPendingInputAction nextPendingInputAction;

    public JenkinsBuildInfo() {
    }

    public JenkinsBuildInfo(String id, String status, Long startTimeMillis, Long durationMillis, String username, String triggerType, String branch) {
        this.id = id;
        this.status = status;
        this.startTimeMillis = startTimeMillis;
        this.durationMillis = durationMillis;
        this.username = username;
        this.triggerType = triggerType;
        this.branch = branch;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public JenkinsPendingInputAction getNextPendingInputAction() {
        return nextPendingInputAction;
    }

    public void setNextPendingInputAction(JenkinsPendingInputAction nextPendingInputAction) {
        this.nextPendingInputAction = nextPendingInputAction;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(Long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(Long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
