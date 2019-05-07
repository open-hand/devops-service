package io.choerodon.devops.infra.dataobject.harbor;


import java.util.*;

/**
 * Created by Sheep on 2019/4/28.
 */
public class Role {

    private List<Integer> roles;
    private String username;

    public List<Integer> getRoles() {
        return roles;
    }

    public void setRoles(List<Integer> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
