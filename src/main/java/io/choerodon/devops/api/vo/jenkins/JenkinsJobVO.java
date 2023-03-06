package io.choerodon.devops.api.vo.jenkins;

import java.util.List;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/3 9:26
 */
public class JenkinsJobVO {

    @Encrypt
    private Long jenkinsServerId;

    private String jenkinsServerName;

    private String type;

    private String folder;
    private String name;

    private String fullName;

    private String url;

    private String status;

    private Long startTimeMillis;
    private Long durationMillis;

    private String username;

    private String triggerType;

    private List<JenkinsJobVO> jobs;

    public JenkinsJobVO() {
    }

    public JenkinsJobVO(Long jenkinsServerId, String jenkinsServerName, String type, String folder, String name, String url) {
        this.jenkinsServerId = jenkinsServerId;
        this.jenkinsServerName = jenkinsServerName;
        this.type = type;
        this.folder = folder;
        this.name = name;
        this.url = url;
        this.fullName = folder + name;
    }

    public List<JenkinsJobVO> getJobs() {
        return jobs;
    }

    public void setJobs(List<JenkinsJobVO> jobs) {
        this.jobs = jobs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getJenkinsServerName() {
        return jenkinsServerName;
    }

    public void setJenkinsServerName(String jenkinsServerName) {
        this.jenkinsServerName = jenkinsServerName;
    }


    public Long getJenkinsServerId() {
        return jenkinsServerId;
    }

    public void setJenkinsServerId(Long jenkinsServerId) {
        this.jenkinsServerId = jenkinsServerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
