package io.choerodon.devops.domain.application.entity.gitlab;

import io.choerodon.devops.infra.common.util.enums.AccessLevel;

/**
 * Created by Zenger on 2018/3/28.
 */
public class GitlabGroupMemberE {

    private Integer id;
    private String userName;
    private int accessLevel;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public void initAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel.toValue();
    }
}
