package io.choerodon.devops.api.vo;

import org.hibernate.validator.constraints.Length;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.jenkins.JenkinsPluginInfo;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:51
 * Description:
 */
public class DevopsJenkinsServerVO extends AuditDomain {
    @Encrypt
    private Long id;

    private Long projectId;

    @Length(message = "error.devops.jenkins.server.name.length", max = 100, min = 1)
    private String name;

    @Length(message = "error.devops.jenkins.server.url.length", max = 1024, min = 1)
    private String url;

    @Length(message = "error.devops.jenkins.server.description.length", max = 500, min = 1)
    private String description;

    @Length(message = "error.devops.jenkins.server.username.length", max = 48, min = 1)
    private String username;

    @Length(message = "error.devops.jenkins.server.password.length", max = 100, min = 1)
    private String password;

    private String status;

    private JenkinsPluginInfo jenkinsPluginInfo;

    public JenkinsPluginInfo getJenkinsPluginInfo() {
        return jenkinsPluginInfo;
    }

    public void setJenkinsPluginInfo(JenkinsPluginInfo jenkinsPluginInfo) {
        this.jenkinsPluginInfo = jenkinsPluginInfo;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
