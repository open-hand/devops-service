package io.choerodon.devops.api.vo.iam;

import java.util.List;

public class ProjectWithRoleVO {

    private String name;

    private List<RoleVO> roles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoleVO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleVO> roles) {
        this.roles = roles;
    }
}
