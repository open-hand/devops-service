package io.choerodon.devops.api.vo.jenkins;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/3 9:26
 */
public class JenkinsJobVO {

    @ApiModelProperty("Jenkins Server Id")
    @Encrypt
    private Long jenkinsServerId;
    @ApiModelProperty("Jenkins Server 名称")
    private String jenkinsServerName;
    @ApiModelProperty("Job类型：")
    private String type;
    @ApiModelProperty("Job所属目录")
    private String folder;
    @ApiModelProperty("Job名称")
    private String name;
    @ApiModelProperty("Job全称，folder/name")

    private String fullName;
    @ApiModelProperty("Jenkins地址")

    private String url;
    @ApiModelProperty("最近执行状态")

    private String status;
    @ApiModelProperty("最近执行开始时间")

    private Long startTimeMillis;
    @ApiModelProperty("最近执行持续时长")
    private Long durationMillis;
    @ApiModelProperty("最近执行用户")

    private String username;
    @ApiModelProperty("触发方式")

    private String triggerType;
    @ApiModelProperty("下级Job，多分支流水线时存在")

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
