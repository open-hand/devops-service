package io.choerodon.devops.api.dto;

import java.io.Serializable;
import java.util.Objects;

import com.alibaba.fastjson.JSON;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/13
 */
public class ProjectConfigDTO implements Serializable {

    private String url;

    private String userName;

    private String password;

    private String project;

    private String email;

    private Boolean isPrivate;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return JSON.toJSONString(this).equals(JSON.toJSONString(o));
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, userName, password, project, email, isPrivate);
    }
}
