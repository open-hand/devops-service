package io.choerodon.devops.api.vo.jenkins;

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

    private String url;

    public JenkinsJobVO() {
    }

    public JenkinsJobVO(Long jenkinsServerId, String jenkinsServerName, String type, String folder, String name, String url) {
        this.jenkinsServerId = jenkinsServerId;
        this.jenkinsServerName = jenkinsServerName;
        this.type = type;
        this.folder = folder;
        this.name = name;
        this.url = url;
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
