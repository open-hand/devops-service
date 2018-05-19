package io.choerodon.devops.infra.dataobject.gitlab;

import java.util.Date;

import io.choerodon.devops.infra.common.util.enums.AccessLevel;

/**
 * Created by Zenger on 2017/11/14.
 */
public class MemberDO {

    private AccessLevel accessLevel;
    private Date createdAt;
    private String email;
    private Integer id;
    private String name;
    private String state;
    private String username;

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
